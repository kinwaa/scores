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

## 注意事项
1. 单个文件双面打印时，公司打印机会把第一页印在上方
2. 如果不是双面打印的时候，会打印在下方
