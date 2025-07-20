# 赞美诗乐谱

## 制作要求
1. 字体暂用楷体
   1. 歌名用26点，副标题用17点
   2. 作家等用12点
   3. 歌词用15点
   4. 页脚用12点
2. 红色赞美诗的页脚，中间放“赞美诗 ·新编·”，外边放页码
3. 天乐诗班的页脚，外面放“天乐诗班 yyyy年M月d日 献（也就是rights）”
4. 页边距用12mm，装订边加4mm
5. 其余按musicxml文件中的备注
6. 可以适当调整"Staff space"来控制分页排版

## musicxml文件规范
### 文件root
``` xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<score-partwise
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xmlns="http://www.musicxml.org/xsd/MusicXML"
   xsi:schemaLocation="
      http://www.musicxml.org/xsd/MusicXML
      https://i.kinwaa.cn/schema/musicxml-4.0/musicxml.xsd"
   version="4.0">

   ...
</score-partwise>
```

## 生成乐谱规范（步骤）
1. 从musescore导出musicxml格式的文件;
2. 更正xml头；
3. 执行ScoreToastApplication，初步自动处理文件内容；
4. 合唱谱的part-group中，增加“<group-barline>no</group-barline>”（如果有symbol为square的part-group，可以去掉，整对去掉）；
5. 更正part-name，例如：Soprano&#xA0;&amp;&#xA0;Alto、Tenor&#xA0;&amp;&#xA0;Base；
6. 修改cresc.、rit.的显示（有需要的话，后面跟着的dashes也可以清除掉）；
7. 对整个文件格式化，删掉空行、删掉缩进；
8. 留意乐谱内的注意说明。

## 注意事项
1. 单个文件双面打印时，公司打印机会把第一页印在上方
2. 如果不是双面打印的时候，会打印在下方
