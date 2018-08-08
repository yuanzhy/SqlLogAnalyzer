package com.yuanzhy.tools.sql.classify.impl;

import com.yuanzhy.tools.sql.classify.BaseClassifier;
import com.yuanzhy.tools.sql.classify.IClassifier;
import com.yuanzhy.tools.sql.model.SqlLog;

import java.util.regex.Pattern;

/**
 * 按SQL结构分类（sql结构相同，参数不同的算一类）
 *
 * @author yuanzhy
 * @date 2018/6/13
 */
public class StructureClassifier extends BaseClassifier implements IClassifier {

    protected static final Pattern PARAM_PATTERN = Pattern.compile("(?<==\\s{0,3})[\\w_']+|(?<=in\\s{0,3}\\()[\\w\\s',]+|(?<=>\\s{0,3})[\\w_']+|(?<=<\\s{0,3})[\\w_']+");

    @Override
    protected String getClassifyKey(SqlLog sqlLog) {
        return PARAM_PATTERN.matcher(sqlLog.getSql()).replaceAll("?");
    }

    public static void main(String[] a) {
        String sql = "select count(tspgljgsj0_.C_BH) as col_0_0_, isnull(sum(case when tspgljgsj0_.N_DQJKZT=4 then 1 else 0 end), 0) as col_1_0_, isnull(sum(case when tspgljgsj0_.N_DQJKZT=5 \n" +
                "then 1 else 0 end), 0) as col_2_0_, isnull(sum(case when tspgljgsj0_.N_DQJKZT=3 then 1 else 0 end), 0) as col_3_0_ from YWST.dbo.T_SPGL_JGSJ tspgljgsj0_ \n" +
                "where (tspgljgsj0_.N_AJLB='8' or tspgljgsj0_.N_AJLB=18 and tspgljgsj0_.N_SPCX=8) and (tspgljgsj0_.N_DQJKZT in (3 , 4 , 5)) and tspgljgsj0_.N_AJJZJD>=7 and \n" +
                "tspgljgsj0_.N_AJJZJD<=10 and tspgljgsj0_.N_CBR=157466028 and tspgljgsj0_.N_FY<>2402 and a > 5 and a< xxs";
        Pattern p = Pattern.compile("(?<==\\s{0,3})[\\w_']+|(?<=in\\s{0,3}\\()[\\w\\s',]+|(?<=>\\s{0,3})[\\w_']+|(?<=<\\s{0,3})[\\w_']+");
        String result = p.matcher(sql).replaceAll("?");
        System.out.println(result);
    }
}
