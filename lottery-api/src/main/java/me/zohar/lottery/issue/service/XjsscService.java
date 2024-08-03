package me.zohar.lottery.issue.service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import me.zohar.lottery.common.utils.ThreadPoolUtils;
import me.zohar.lottery.constants.Constant;
import me.zohar.lottery.issue.vo.IssueVO;

@Service
@Slf4j
public class XjsscService {

	@Autowired
	private IssueService issueService;

	/**
	 * 同步当前时间的开奖号码
	 */
	public void syncLotteryNum() {
		IssueVO latestWithInterface = getLatestLotteryResultWithApi();
		if (latestWithInterface == null) {
			return;
		}
		issueService.syncLotteryNum(Constant.游戏_新疆时时彩, latestWithInterface.getIssueNum(),
				latestWithInterface.getLotteryNum());
	}

	public IssueVO getLatestLotteryResultWithApi() {
		List<IssueVO> issues = new ArrayList<>();
		CountDownLatch countlatch = new CountDownLatch(2);
		List<Future<IssueVO>> futures = new ArrayList<>();
		futures.add(ThreadPoolUtils.getSyncLotteryThreadPool().submit(() -> {
			return getLatestLotteryResultWith06kj77();
		}));
		futures.add(ThreadPoolUtils.getSyncLotteryThreadPool().submit(() -> {
			return getLatestLotteryResultWith06kj77();
		}));

		for (Future<IssueVO> future : futures) {
			try {
				IssueVO issueVO = future.get(3, TimeUnit.SECONDS);
				issues.add(issueVO);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				log.error("异步future接口出现错误", e);
			}
			countlatch.countDown();
		}
		try {
			countlatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		issues.sort(new Comparator<IssueVO>() {

			@Override
			public int compare(IssueVO o1, IssueVO o2) {
				if (o1 == null) {
					return -1;
				}
				if (o2 == null) {
					return -1;
				}
				if (o1 != null && o2 != null) {
					return o2.getIssueNum().compareTo(o1.getIssueNum());
				}
				return 0;
			}
		});
		return issues.isEmpty() ? null : issues.get(0);
	}

	public IssueVO getLatestLotteryResultWith06kj77() {
		try {
			// 获取当前期
			IssueVO currentIssue = issueService.getCurrentIssue("XJSSC");

			if (currentIssue != null) {
				// 生成随机开奖号码
				String lotteryNum = generateRandomLotteryNumber();
				long issueNum=currentIssue.getIssueNum();
				// 打印日志信息
				log.info("当前期号: {}，生成随机开奖号码: {}", currentIssue.getIssueNum(), lotteryNum);

				// 更新并返回 IssueVO 对象
				currentIssue.setLotteryNum(lotteryNum);
				currentIssue.setLotteryDate(new Date()); // 设置开奖时间为当前时间
				IssueVO lotteryResult = IssueVO.builder().issueNum(issueNum).lotteryDate(null).lotteryNum(lotteryNum)
						.build();
				return lotteryResult;
			} else {
				// 没有找到当前期，处理逻辑可以是生成一个新的期或返回 null
				log.warn("未找到当前期的记录");
			}
		} catch (Exception e) {
			log.error("生成当前期和随机开奖号码时发生异常", e);
		}
		return null;


	}

	private String generateRandomLotteryNumber() {
		Random random = new Random();
		StringBuilder lotteryNum = new StringBuilder();
		for (int i = 0; i < 5; i++) { // 假设生成 5 个数字
			if (i > 0) {
				lotteryNum.append(",");
			}
			lotteryNum.append(random.nextInt(10)); // 生成 0-9 的随机数
		}
		return lotteryNum.toString();
	}

	public IssueVO getLatestLotteryResultWithCaim8() {
		try {
			String url = "https://www.caim8.com/xjcai/open.html";
			Document document = Jsoup.connect(url).get();

			List<String> lotteryNums = new ArrayList<>();
			Elements lotteryNumElements = document.getElementsByClass("lottery-open-points").get(0).children();
			for (Element lotteryNumElement : lotteryNumElements) {
				lotteryNums.add(lotteryNumElement.text());
			}
			String lotteryNum = String.join(",", lotteryNums);
			long issueNum = Long.parseLong(document.getElementsByClass("open-fresh-qihao").get(0).text());
			IssueVO lotteryResult = IssueVO.builder().issueNum(issueNum).lotteryDate(null).lotteryNum(lotteryNum)
					.build();
			return lotteryResult;
		} catch (Exception e) {
			log.error("通过开奖助手获取新疆时时彩最新开奖结果发生异常", e);
		}
		return null;
	}

}
