# NestedScrolledRecyclerView
自定义Behavior+两个Recyclerview实现嵌套滚动，实现仿微博发现效果

微博发现页包括其他类似的实现方式，大多为顶部普通ViewGroup(如LinearLayout)+底部RecyclerView的方式实现
如果我们需要在顶部同时支持长内容的列表RecyclerView + 底部长内容RecyclerView
就可以使用自定义顶部Behavior + 底部Behavior,
使用偏移的方式实现嵌套滚动，同时手势连贯不断层，极大的保证交互体验！

原理及实现方式介绍在这里：
https://www.jianshu.com/p/ffe13743771d


![image](https://github.com/879058443/NestedScrollRecyclerView/blob/master/gif/gifhome_320x568_20s.gif) 
