package study.querydsl.repository;

import net.minidev.json.JSONValue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberQueryRepository memberQueryRepository;

    @Test
    public void basicTest(){

        Member member = new Member("member1",10);

        memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberRepository.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberRepository.findByUsername(member.getUsername());
        assertThat(result2).containsExactly(member);

        List<Member> result3 = memberRepository.findAll();
        assertThat(result3).containsExactly(member);

        List<Member> result4 = memberRepository.findByUsername(member.getUsername());
        assertThat(result4).containsExactly(member);
    }

    @Test
    public void searchTest() {

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();
        em.clear();

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        List<MemberTeamDto> result2 = memberRepository.search(condition);
        assertThat(result2).extracting("username").containsExactly("member4");

        List<MemberTeamDto> result3 = memberQueryRepository.search(condition);
        assertThat(result3).extracting("username").containsExactly("member4");

    }

    @Test
    public void searchPageSimple() {

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();
        em.clear();

        MemberSearchCondition condition = new MemberSearchCondition();

        PageRequest request = PageRequest.of(0, 3);

        Page<MemberTeamDto> memberTeamDtos = memberRepository.searchPageSimple(condition, request);

        assertThat(memberTeamDtos.getContent()).extracting("username").containsExactly("member1", "member2", "member3");
        assertThat(memberTeamDtos.getSize()).isEqualTo(3);
    }

    @Test
    public void searchPageComplex() {

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();
        em.clear();

        MemberSearchCondition condition = new MemberSearchCondition();

        PageRequest request = PageRequest.of(0, 3);

        Page<MemberTeamDto> memberTeamDtos = memberRepository.searchPageComplex(condition, request);

        assertThat(memberTeamDtos.getContent()).extracting("username").containsExactly("member1", "member2", "member3");
        assertThat(memberTeamDtos.getSize()).isEqualTo(3);
    }

    @Test
    void querydslPredicateExecutorTest() {

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();
        em.clear();

        QMember member = QMember.member;
        Iterable<Member> result = memberRepository.findAll(
                    member.age.between(20,40)
                            .and(member.username.eq("member1"))
        );
        for (Member findMember : result) {
            System.out.println("findMember = " + findMember);
        }
    }
}