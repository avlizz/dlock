# dlock
方法上加个注解实现分布式锁功能，支持从方法的参数获取值组成更细粒度的锁，支持阻塞等待，尝试获取锁失败退出方法  
依赖：  
    1.spring
    2.spring el  
    3.spring aop  
    4.redisson  

如下例子： 
@DLock(name="user",expression = "#userReq.id")  
public UserResp queryUser(UserReq userReq) {  
    UserResp userResp = new UserResp();  
    userResp.setId(userReq.getId());  
    return userResp;  
}  
请求参数：userReq的id=1，那么分布式锁的名称是：user1，userReq的id=2，那么分布式锁的名称是: user2  
