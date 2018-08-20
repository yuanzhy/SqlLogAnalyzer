package com.yuanzhy.tools.sql;

import com.yuanzhy.tools.sql.classify.ClassifierFactory;
import com.yuanzhy.tools.sql.classify.IClassifier;
import com.yuanzhy.tools.sql.common.model.SqlLog;
import com.yuanzhy.tools.sql.common.util.ArgumentUtil;
import com.yuanzhy.tools.sql.common.util.StorageUtil;
import com.yuanzhy.tools.sql.execute.ExecutorFactory;
import com.yuanzhy.tools.sql.execute.IExecutor;
import com.yuanzhy.tools.sql.input.IInput;
import com.yuanzhy.tools.sql.input.InputFactory;
import com.yuanzhy.tools.sql.output.IOutput;
import com.yuanzhy.tools.sql.output.OutputFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 为了兼容低版本环境，使用JDK6开发
 *
 * @author yuanzhy
 * @date 2018/6/13
 */
public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);

    /**
     * 日志路径不传默认为jar包所在路径
     *
     * @param args ["logPath"]，相对于jar所在目录的路径, 也可传递绝对路径
     */
    public static void main(String[] args) {
//        args = new String[]{"F:\\NP\\河南2018-8-6\\sym"}; // test
        ArgumentUtil.parseArgs(args);
        String path = ArgumentUtil.getArgument("path");
        // 读取数据源，抽象，持有文件流引用方式
        IInput input = InputFactory.newInstance(path);
        // SQL分组, 为提升处理性能，暂实现为分组后每组只取一条存储在内存中（否则全部存储太大，需要落地到文件了）
        log.info("============开始分类");
        IClassifier classifier = ClassifierFactory.newInstance();
        List<SqlLog> sqlLogs = classifier.doClassify(input);
        // 每组选取一条到数据库中执行一下，记录执行时间等
        log.info("============执行分类SQL");
        IExecutor executor = ExecutorFactory.newInstance();
        executor.doExecute(sqlLogs);
        // 按SQL执行时间倒叙排列下，出一个简易报告，输出到文件
        IOutput output = OutputFactory.newInstance();
        output.doOutput(sqlLogs);
    }
}
