package me.zohar.lottery.issue.repo;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import me.zohar.lottery.issue.domain.Issue;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IssueRepo extends JpaRepository<Issue, String>, JpaSpecificationExecutor<Issue> {

	List<Issue> findByGameCodeAndLotteryDateAndLotteryTimeGreaterThanEqualOrderByLotteryTimeAsc(String gameCode,
			Date lotteryDate, Date lotteryTime);

	List<Issue> findByGameCodeAndLotteryDateOrderByLotteryTimeDesc(String gameCode, Date lotteryDate);

	/**
	 * 根据游戏,开始时间,结束时间获取开奖结果
	 * 
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	Issue findTopByGameCodeAndStartTimeLessThanEqualAndEndTimeGreaterThan(String gameCode, Date startTime,
			Date endTime);

	Issue findByGameCodeAndIssueNum(String gameCode, Long issueNum);

	Issue findTopByGameCodeAndIssueNumLessThanOrderByIssueNumDesc(String gameCode, Long issueNum);

	List<Issue> findTop5ByGameCodeAndEndTimeLessThanOrderByIssueNumDesc(String gameCode, Date endTime);

	List<Issue> findTop50ByGameCodeAndEndTimeLessThanOrderByIssueNumDesc(String gameCode, Date endTime);

	/**
	 * 获取下一期
	 * 
	 * @param startTime
	 * @return
	 */
	Issue findTopByGameCodeAndStartTimeGreaterThanOrderByLotteryTimeAsc(String gameCode, Date startTime);

	Issue findTopByGameCodeAndEndTimeLessThanEqualOrderByEndTimeDesc(String gameCode, Date now);
	@Query("SELECT i FROM Issue i WHERE i.gameCode = :gameCode AND i.startTime > :currentTime ORDER BY i.startTime ASC")
	Issue findUpcomingIssue(@Param("gameCode") String gameCode, @Param("currentTime") Date currentTime);
	Issue findTopByGameCodeAndStartTimeLessThanEqualAndEndTimeGreaterThanAndStateOrderByEndTimeAsc(
			String gameCode, Date startTime, Date endTime, int state);


}
