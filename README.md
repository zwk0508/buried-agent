# buried-agent
埋点agent
###### 基于Java字节码技术实现的可配置、收集数据并执行处理器方法的agent
### 用法
项目启动时添加javaagent： -javaagent:agent.jar=<agent.conf的地址>读取配置文件的方式是 new File("path")
### 实现方式
把需要收集数据的方法重命名为：` ___auto_generate_method_ ` + `方法名` + `后缀`，重新实现同名方法，并在新方法上生成@GeneratedMethod注解，
用于查找原来的方法，获取方法上的注解信息
```java
 private void ___auto_generate_method_forTest_1(Object a, byte b, short s, int i, long j, float f, double d, char c, boolean z) {
        System.out.println(a);
    }

    @GeneratedMethod(
        value = "___auto_generate_method_forTest_1",
        descriptor = "(Ljava/lang/Object;BSIJFDCZ)V"
    )
    public void forTest(Object var1, byte var2, short var3, int var4, long var5, float var7, double var8, char var10, boolean var11) {
        try {
            this.___auto_generate_method_forTest_1(var1, var2, var3, var4, var5, var7, var8, var10, var11);
        } catch (Throwable var16) {
            Object[] var14 = new Object[]{var1, var2, var3, var4, var5, var7, var8, var10, var11};
            MyHandler.__akhfga(this, (Object)null, var16, var14);
            throw var16;
        }

        Object[] var12 = new Object[]{var1, var2, var3, var4, var5, var7, var8, var10, var11};
        MyHandler.__akhfga(this, (Object)null, (Object)null, var12);
    }
```
### 配置文件内容
```code

 # 配置文件支持的指令
 # 1、handler 收集到的收据处理器，请最多声明一个处理器，否则只采用最后一个
 # 声明格式： handler fullQualifiedClassName.methodName
 #      handler 指令后跟参数，以空格分割，类的全限定名.方法名
 #      例如：com.zwk.handler.ParameterHandler.handle
 #      方法的签名必须是四个参数：
 #      java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object[]
 #      第一个参数： target 即方法执行所在类，当方法是静态类型时，则target值为null
 #      第二个参数： ret 即方法返回值，当方法返回void或null时，值为null
 #      第三个参数： exception 即方法抛出的异常，没有异常时，值为null
 #      第四个参数： params 即方法的参数
 # ---------------------------------------------------------------------------------------------------------------------
 # 2、method  问号代表0个或一个，即可有可无
 # 声明格式：method modifier? ret-type declare-type method-name params throws? (handler fullQualifiedClassName.methodName)?
 # 1）、method 指令后跟参数，以空格分割
 # 2）、modifier 方法修饰符 有以下组合
 #      - public static
 #      - public final
 #      - private static
 #      - protected static
 #      - protected final
 #      - static
 #      - final
 # 3）、ret-type 方法返回值类型，类如：com.zwk.model.User or com.zwk.model.* or com.zwk..*， 基本类型 int long...
 # 4）、declare-type 声明的类型 同返回值类型
 # 5）、method-name 方法名称 可以是*代表任意方法名 可以前缀星号，也可以后缀星号 例如：*Test 以Test结尾、 get* 以get开头
 # 6）、params 参数 多个参数以逗号分隔 例如
 #      - **                        匹配任意参数
 #      - java.lang.Object,int      匹配Object和int
 #      - com.zwk.model.*,int       匹配com.zwk.model包下的任意类型和int
 #      - com.zwk..*,int            匹配com.zwk包及子包下的任意类型和int
 #      - com.zwk.model.*,int,**    前两个参数匹配com.zwk.model包下的任意类和int，后面为任意参数
 # 7）、throws 异常 多个异常以逗号分隔 参见params的声明方式
 # 8）、指定handler则采用当前的handler
 # ---------------------------------------------------------------------------------------------------------------------
 # 3、target 目标类
 # 声明格式：target fullQualifiedClassName (handler fullQualifiedClassName.methodName)?
 # 类的全限定名，多个以逗号分隔，指定handler则采用当前的handler
 #      - com.zwk.model.User        匹配com.zwk.model.User类型
 #      - com.zwk.model.*           匹配com.zwk.model包下的任意类型
 #      - com.zwk..*                匹配com.zwk包及子包下的任意类型
 # ---------------------------------------------------------------------------------------------------------------------
 # 4、@target 目标类上的注解，参见target的声明
 # 声明格式：@target fullQualifiedClassName (handler fullQualifiedClassName.methodName)?
 # 注解的全限定名，多个以逗号分隔，指定handler则采用当前的handler
 # ---------------------------------------------------------------------------------------------------------------------
 # handler使用的优先级问题
 # 当不同指令匹配时，优先级：target > @target > method
 # 同样指令匹配多匹配时：优先选用最先声明的指令，当两个target匹配时，则采用优先声明的target
 # ---------------------------------------------------------------------------------------------------------------------
 # 匹配的是com.zwk.model.User类型下的 public static 修饰符的 以Test 结尾的 任意参数的方法
 method public static * com.zwk.model.User *Test **
 # 收集到的数据是执行 com.zwk.handler.ParameterHandler 类的handle方法
 handler com.zwk.handler.ParameterHandler.handle 
```