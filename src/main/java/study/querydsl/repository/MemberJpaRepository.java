package study.querydsl.repository;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

import java.util.List;
import java.util.Optional;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@Repository
//@RequiredArgsConstructor
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager em) { this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    public List<Member> findAll_Querydsl() {
        return queryFactory
                .selectFrom(member).fetch();
    }

    public List<Member> findByUsername_Querydsl(String username) {
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition searchCondition) {

        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(searchCondition.getUsername())) {
            builder.and(member.username.eq(searchCondition.getUsername()));
        }

        if (StringUtils.hasText(searchCondition.getTeamName())) {
            builder.and(team.name.eq(searchCondition.getTeamName()));
        }

        if (searchCondition.getAgeGoe() != null) {
            builder.and(member.age.goe(searchCondition.getAgeGoe()));
        }

        if (searchCondition.getAgeLoe() != null) {
            builder.and(member.age.loe(searchCondition.getAgeLoe()));
        }

            return queryFactory
                    .select(new QMemberTeamDto(member.id, member.username, member.age, team.id, team.name))
                    .from(member)
                    .leftJoin(member.team, team)
                    .where(builder)
                    .fetch();
    }

}
