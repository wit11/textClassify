package com.fmyblack.textClassify.cosines.single;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fmyblack.common.FileReaderUtil;
import com.fmyblack.textClassify.tfidf.Document;
import com.fmyblack.textClassify.tfidf.TagBase;
import com.fmyblack.word.Rmm;

public class CosinesModel {

	List<TagBase> tags;
	TFIDF tfidf;
	Map<String, Double> idf;
	Rmm rmm;
	
	private CosinesModel(){
		rmm = new Rmm();
	};
	
	public static CosinesModel train(String seedsDir) {
		CosinesModel cm = new CosinesModel();
		
		Seeds seeds = new Seeds(seedsDir);
		cm.tags = seeds.getTags();
		int documentsNum = seeds.getDocumentsNum();
		Set<String> words = seeds.getWords();
		cm.tfidf = new TFIDF(documentsNum, words);
		cm.idf = cm.tfidf.idf(cm.tags);
		for(TagBase tagBase : cm.tags) {
			Tag tag = (Tag) tagBase;
			tag.train(cm.idf);
		}
		
		return cm;
	}
	
	public Result classify(String text) {
		List<String> words = rmm.rmmSegment(text);
		Document doc = new Document(words);
		Map<String, Integer> wordsFre = doc.getWordsFrequency();
		Result currentResult = null;
		double maxAccuracy = 0.0;
		Set<String> trainWords = this.tfidf.getWords();
		for(TagBase tagBase : this.tags) {
			Tag tag = (Tag) tagBase;
			double accuracy = tag.caculateAccuracy(this.idf, wordsFre);
//			System.out.println(tmpResult.toString());
			if(accuracy > maxAccuracy) {
				currentResult = new Result(tag.name,accuracy);
				maxAccuracy = accuracy;
			}
		}
		return currentResult;
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		String dir = "/Users/fmyblack/javaproject/textClassify/src/main/resources/data";
		String seedsDir = dir + File.separator + "nbc_seeds";
		String resultOut = dir + File.separator + "result.out";
		CosinesModel cm = CosinesModel.train(seedsDir);
//		cm.save(resultOut);
////		
//		CosinesModel newNbm = CosinesModel.load(resultOut);
//		
        test(cm, seedsDir);
        testNewFile(cm);
	}
	
	public static void testNewFile(CosinesModel nbm) {
		String text = ""
				+ "贫困地区发展特色产业促进精准脱贫指导意见"
				+ "国家统计局于22日发布了31个省份2016年的地区生产总值(GDP)数据。"
				+ "从经济总量上来看，广东、江苏不出意外地牢牢占据着排名第一、第二的位置，不过广东的第一经济大省位子更稳了;在名义增速方面，则呈现了南快北慢的趋势，西南地区的西藏、贵州和重庆均超过了11%，位列前三。"
				+ "值得一提的是，在“挤水分”后，辽宁在全国的GDP排名，从之前的第10名下滑到14名，排在了中部省份安徽的后面。"
				+ "1月17日，辽宁省委副书记、省长陈求发在辽宁省十二届人大八次会议上作政府工作报告时首次确认，辽宁省所辖市、县，在2011年至2014年存在财政数据造假的问题，指出在2011年至2014年“官出数字、数字出官”，导致经济数据被注入水分。"
				+ "辽宁省财政厅数据显示，2015年全省一般公共预算收入2125.6亿元，下降33.4%，其中税收收入1650.2亿元，下降29.2%;非税收入475.4亿元，下降44.9%。辽宁省财政厅当时对此的解释是，造成全省财政收入大幅下降的主要原因中，排在首位的是按“三严三实”依法依规组织财政收入，做实了财政收入的影响。";
//				+ "简章规定，从今年开始，国际学生不需要考试，只要通过HSK汉语水平测试，就可以申请清华大学本科。而以往所有申请清华本科的国际学生，都需要先参加4至5个科目的考试，然后再加试或面试后录取。"
//				+ "网友为此惊呼：“十年寒窗不如一纸国籍，清华新政让中国考生欲哭无泪！”人们担心，清华为外籍学生设置的入学门槛太低，对国内考生不公平。更担心的是，清华新政一出，有条件的家长，完全可以让子女放弃中国国籍，以“移民”的方法曲线上清华。"
//				+ "据报道，很多东南亚或非洲小国移民中介机构，已经打出了“免试上清华”的宣传口号，提供整体解决方案。"
				;
				Result r = nbm.classify(text);
		System.out.println(r.toString());
	}
	
	public static void test(CosinesModel nbm, String dir) {
		File seeds = new File(dir);
		int all = 0;
		int right = 0;
		for(String seed : seeds.list()) {
			String newDir = dir + File.separator + seed;
			File s = new File(newDir);
			for(String textName : s.list()) {
				String text = FileReaderUtil.readFile(newDir + File.separator + textName);
				Result r = nbm.classify(text);
				System.out.println(seed + "\t" + r.toString());
				all++;
				if( seed.equals(r.getTag())) {
					right++;
				}
			}
		}
		System.out.println("all:\t" + all);
		System.out.println("right:\t" + right);
	}
}