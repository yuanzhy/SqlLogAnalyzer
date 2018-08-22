# SqlLogAnalyzer 

### 分析SQL日志

- 支持sqlOnly，jdbc日志
- 支持按sql结构分类，同一秒同线程SQL情况分类
- 支持连接到数据库中执行一遍查看执行速度，可配置并发执行
- 分析结果支持多格式输出
- 支持磁盘缓存（解决大量日志的情况堆内存不够的情况，但会较低分析速度）
- 以上所有功能可配置

### 后续规划
- 并发读取文件 or 并发分类，提升分析速度
- 支持更多分类方式，输出格式等
- 支持图形界面操作配置
