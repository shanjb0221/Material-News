# Material News

Material Design 风格的 Android 新闻 App

### 1 代码结构

`/app/src/main/`

#### 1.1 Java代码部分

`./java/com/java/shanjingbo/`

- `activity/`：程序入口 `Activity`
- `adapter/`：`RecyclerView` 适配器，适配 Data-Binding 的 `ViewHolder`
- `bean/`：新闻、网络相应 Bean
- `channel_editor/`：频道管理器相关代码
  【部分代码参考：[CSDN](https://blog.csdn.net/number_cmd9/article/details/119118035)
  | [GITEE](https://gitee.com/jiugeishere/uidesign)】
- `channel_pager/`：首页频道分类 `TabLayout` & `ViewPager2` 的封装
- `constants/`：可选频道、每页新闻条数等常量
- `fragment/`：主页、列表、搜索、详情、历史记录、收藏 `Fragment`
- `services/`：图片加载服务，抽象 `Pager`
  - `database/`：数据库服务，数据库 `Pager`封装
  - `web/`：网络服务，网络 `Pager` 封装
- `utils/`：`image`
  字段拆分工具，视频自动播放帮助类【参考：[GitHub: GSYVideoPlayer](https://github.com/CarGuo/GSYVideoPlayer/)】，时间格式工具【部分代码参考：[CSDN](https://blog.csdn.net/qq_34492495/article/details/89671496)】

#### 1.2 xml代码部分

`./res/`

- `anim/`：自定义界面切换动画
- `color/`：自定义颜色选择器
- `drawable/`：图标、页面背景
- `layout/`：页面布局
- `menu/`：菜单项配置
- `navigation/`：页面导航
- `values/`：字符串、主题配置
- `xml/`：网络、备份等配置

#### 1.3 其他文件

- `/app/main/AndroidManifest.xml`：应用程序清单

#### 1.4 项目依赖

`/app/build.gradle`

- Navigation：页面导航

  ```
  def nav_version = "2.3.5"
  implementation "androidx.navigation:navigation-fragment:$nav_version"
  implementation "androidx.navigation:navigation-ui:$nav_version"
  implementation "androidx.navigation:navigation-dynamic-features-fragment:$nav_version"
  ```

- Glide：图片加载

  ```
  implementation 'com.github.bumptech.glide:glide:4.12.0'
  annotationProcessor 'com.github.bumptech.glide:compiler:4.12.0'
  ```

- Room：`SQLite` 操作

  ```
  def room_version = "2.3.0"
  implementation "androidx.room:room-runtime:$room_version"
  annotationProcessor "androidx.room:room-compiler:$room_version"
  implementation "androidx.room:room-guava:$room_version"
  ```

- Retrofit：网络通信

  ```
  implementation 'com.squareup.retrofit2:retrofit:2.9.0'
  implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
  ```

- Gson：`json` 解析

  ```
  implementation 'com.google.code.gson:gson:2.8.8'
  ```

- GSYVideoPlayer：视频播放

  ```
  implementation 'com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer:v8.1.5-jitpack'
  ```

- BackdropLayout：Material 风格 `backdrop` 布局

  ```
  implementation 'com.github.roiacult:BackdropLayout:2.3'
  ```

### 2 具体实现

#### 2.1 用户界面部分

采用单 `Acticity` 多 `Fragment` 模式设计。

`Fragment` 之间通过 `Navigation` 及 `NavigationUI`（应用栏、导航界面）控制切换。

页面元素获取采用了 Data-Binding 技术。

##### 2.1.1 主页面（`MainFragment`）

总体采用 `DrawerLayout` 布局，主要分为 4 部分：

- **新闻列表界面**：采用 `TabLayout` 与 `ViewPager2` 联动方式设计，每个频道对应一个子列表页面。
- **应用栏**：与新闻列表共同包含在一个 `CoordinatorLayout` 中，当新闻列表界面向上滑动时，应用栏随之收起。
- **导航界面**：点击应用栏左侧按钮或侧滑打开，包含程序中各个目的地的入口及清空历史记录等功能按钮。
- **频道编辑界面**：使用 `BottomSheetBehavior`
  从底部展开；参考[开源代码](https://gitee.com/jiugeishere/uidesign)，采用 `RecyclerView` 排列各个频道，采用 `TouchHelper`
  、`TransitionAnimator` 实现拖动排序及动画（修复了开源代码中的部分 bug : (）；频道列表使用 `SharedPreference`
  保存在应用数据中；当频道列表发生变化时，执行 `SharedPreference` 写入并通知 `ViewPager2` 更新。

##### 2.1.2 列表页面（`ListFragment`）

使用 `RecyclerView` 实现，是主页面、搜索页面、历史记录页面、收藏列表页面的内嵌页面，支持下拉刷新与上拉加载：

- **下拉刷新**：使用外层包裹的 `SwipeRefreshLayout` 原生实现；刷新时根据嵌入的页面类型（及检索参数）构造 `Pager` 并查询第 1 页。
- **新闻条目**：使用 `CardView` 展示了新闻的标题、发布者、发布时间、图片/视频、已读、收藏信息；通过自定义 `viewType`，实现无图、单图、多图 ($\geq 3$)、视频 4
  种显示模式。
- **上拉加载**：在 `RecyclerView` 中新闻条目之后加入了一个 `loader` 占位；通过监听滚动事件，当 `loader` 在页面底部可见时触发 `Pager` 加载下一页。

##### 2.1.3 搜索页面（`SearchFragment`）

使用 `BackDropLayout` 实现，有前后两层：

- **搜索结果界面**（前）：主体部分嵌入列表页面，顶部展示搜索到的结果条数；主体部分可以暂时折叠以展示高级高级搜索界面。
- **高级搜索界面**（后）：关键词在应用栏中的搜索组件中输入；频道在下拉列表中选择；时间范围在带格式匹配的文本框中输入；修改任意搜索条件后都将在结果界面顶部实时展示预计结果条数。

##### 2.1.4 详情页面（`DetailFragment`）

使用 `CoordinatorLayout` 实现，进入时向数据库写入阅读时间：

- **新闻详情**：顶部同时嵌入了默认隐藏的单张图片、滑动式多图、视频播放器组件，使用时按新闻类型显示需要的组件。
- **应用栏**：提供收藏按钮、在浏览器中打开按钮；收藏状态变化时向数据库写入状态。

使用官方的 Safe Args 框架，从其他页面向详情页面以序列化方式传递新闻（`NewsBean`）等信息。

##### 2.1.5 历史记录页面（`HistoryFragment`）与 收藏列表页面（`StarFragment`）

平凡地基于列表页面构建，仅仅把 `Pager` 的数据来源从网络（`WebPager`）替换为本地数据库（`DBPager`）。

#### 2.2 数据处理部分

##### 2.2.1 网络数据获取

使用开源项目 Retrofit 支持。

定义了接口描述新闻 API 的 `WebInterface`；封装成 `WebService` 类，使用单例模式访问。

##### 2.2.2 JSON 数据处理

使用开源项目 GSON 支持，使用 Be JSON 在线工具生成 Bean 类。

GSON 解析器被注册到 Retrofit 中对新闻 API 返回结果进行自动解析。

对于新闻 API 返回结果中 `image` 字段格式混乱（已知存在字符串数组、引号包裹的字符串数组、单个字符串、空串、`"[ , , ,]"`、`[] [] []` 等格式）的问题，解决方案是：

- 重写 GSON 库的 `String` 类型解析器，首先尝试以 `String` 格式解析；若解析失败，再按 `JsonArray` 格式解析，解析后用 `toString()`
  转换为字符串；最终将字符串格式的 `image` 字段存储到 `NewsBean` 中。
- 在需要获取 `image` 字符串中所有图片链接时，首先使用正则表达式 `\[(.*?)\]` 匹配出所有数组，再类似地对单个数组进行进一步解析，最终将解析结果以 `List<String>`
  格式返回。这一功能被封装成 `ImageUtil` 类中的静态方法。

##### 2.2.3 图片加载

使用开源项目 Glide 支持。封装成 `ImageService` 静态类。

##### 2.2.4 视频播放

使用开源项目 GSYVideoPlayer 支持。

参考项目 Demo，通过监听 `RecyclerView` 的滚动事件，实现了屏幕内视频视频自动播放。通过 Safe Args 框架传递视频播放进度，实现从列表页面进入详情页面时保持播放进度。

##### 2.2.5 数据库

使用官方项目 Room 支持，使用 `ListenableFuture` 处理异步操作。

以 `NewsBean` 作为 Entity；实现了抽象类 `NewsDao` 作为 DAO 层；实现了抽象类 `NewsDB` 作为数据库，`NewsDB` 以单例模式提供访问。

数据库操作被封装在 `DBService` 类中，同样以单例模式提供访问。

##### 2.2.6 分页查询管理器

**抽象分页器**（`AbstractPager`）为外部程序进行网络与数据库查询提供了统一的入口。

外部程序在提交分页查询任务时通过 `FutureCallback` 接口提供执行成功与失败的回调函数。

```java
public abstract class AbstractPager {
  protected int curPage, count, pageSize;

  public AbstractPager(int pageSize) {
    this.pageSize = pageSize;
    this.count = -1;
    this.curPage = 0;
  }

  public AbstractPager() {
    this(Constants.pageSize);
  }

  public boolean isLastPage() {
    return curPage * pageSize >= count;
  }

  public int getCount() {
    return count;
  }

  public abstract void nextPage(FutureCallback<List<NewsBean>> callback);
}

// com.google.ommon.util.concurrent.FutureCallback:
public interface FutureCallback<V> {
  void onSuccess(@NullableDecl V result);

  void onFailure(Throwable t);
}
```

对于网络与数据库分页查询，分别实现了继承类 **网络分页器**（`WebPager`）与 **数据库分页器**（`DBPager`），其中都使用了 `ListenableFuture`
等方法将网络/数据库查询转移给其他线程异步执行。特别的，`WebPager` 的 `nextPage` 函数中还将网络请求失败等被 Retrofit
视为正常结束的情况转交给 `callback.onFailure` 处理。 
