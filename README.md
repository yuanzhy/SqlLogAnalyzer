# SqlLogAnalyzer

SQL日志分析工具，用于排查慢SQL、大量重复SQL等引起的性能问题，支持以下功能

- 支持sqlOnly，jdbc日志
- 支持按sql结构分类，同一秒同线程SQL情况分类
- 支持连接到数据库中执行一遍查看执行速度，可配置并发执行
- 分析结果支持多格式输出
- 支持磁盘缓存（解决大量日志的情况堆内存不够的情况，但会降低分析速度）
- 以上所有功能可配置

## 使用说明
1. 分析过程：SQL分类 -> 分类后取一条执行并记录时间 -> 结果输出到日志目录中的result/下

2. 配置详解
- 配置文件：config.properties，配置文件可以放在和jar平级的目录，优先级大于jar内部的config.properties
- 输入配置tools.impl.input，可选值：
    - SqlOnlyLogInput：sqlOnly日志
    - JdbcLogInput：jdbc日志信息
- 分类器配置tools.impl.classifier，可选值：（同类SQL指结构相同参数不同的SQL）
    - SameSecondThreadStructureClassifier：同一秒同线程同类SQL分类器
    - StructureClassifier：同类SQL分类器
- 执行器配置tools.impl.executor（需同时配置数据库相关信息，tools.sql开头的配置），可选值：
    - SingleThreadExecutor：单线程执行器
    - ConcurrentExecutor：并发执行器，可通过tools.impl.executor.thread配置并发线程数
    - NullExecutor：不执行（此项不需要配置数据库信息）
- 输出配置tools.impl.output，可选值：
    - MultiOutput：多重输出，需要同时配置tools.impl.output.multi
    - SameSecondThreadTotalCountFileOutput：同一秒同线程SQL总数倒序
    - TotalCountFileOutput：SQL总数倒序
    - SqlTimingFileOutput：SQL执行时间倒序
    - tools.impl.output.top配置项表示输出结果取前多少个
- tools.impl.enableDiskCache启用磁盘缓存配置
    - 如使用SameSecondThreadStructureClassifier分类器且日志量较大建议开启，否则容易OOM

3. 使用说明
- 直接通过命令执行：java -jar SqlLogAnalyzer-xxx.jar --path=D:/logs --resultFilename=laxt --console

4. 参数详解
   参数通过--{name}={value}传递，所有参数都可以不传
- path：日志所在目录
    - 不传递默认为jar包所在目录
    - 可传递绝对路径，相对路径（相对于SqlLogAnalyzer-xxx.jar的路径）
- resultFilename：分析结果的文件名称
    - 不传递默认取output实现类定义的名称
- startTime：开始时间，分析此时间之后的日志（包含此时间）
    - 支持格式：yyyy-MM-dd HH:mm:ss, yyyy-MM-dd HH:mm, yyyy-MM-dd
    - 注意：此参数包含空格，传递时必须添加双引号
- endTime：结束时间，分析此时间之前的日志（包含此时间）
    - 支持格式：yyyy-MM-dd HH:mm:ss, yyyy-MM-dd HH:mm, yyyy-MM-dd
    - 注意：此参数包含空格，传递时必须添加双引号
- console：为boolean参数，以命令行方式运行此程序
    - 不传递默认以图形界面方式启动**(暂未实现)**


## 后续规划
- 并发读取文件 or 并发分类，提升分析速度
- 支持更多分类方式，输出格式等
- 支持图形界面操作配置

## 维护者

[@yuanzhy](https://github.com/yuanzhy)

## 使用许可

[MIT](LICENSE) © yuanzhy
