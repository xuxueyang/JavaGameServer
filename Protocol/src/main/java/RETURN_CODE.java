public class RETURN_CODE
{
    public static final String SUCCESS = "0——成功";
    //通用的以1开头
    public static final String INPUT_IS_ILLEGAL = "1001——输入非法";

    //login模块以2开头
    public static final String ACCOUNT_IS_NOT_EXIST = "2001——账户不存在";
    public static final String ACCOUNT_NAME_HAS_EXIST = "2004——账户名字已存在";
    public static final String ACCOUNT_HAS_ONLINE = "2005——账户已经在线";
    public static final String ACCOUNT_IS_NOT_MATCH = "2006——账户和密码不匹配";
    //mainRoom模块
    public static final String MAIN_ROOM_FRIEND_ADD_ROLE_NOT_EXIST = "3001——添加的好友不存在";
    public static final String ACCOUNT_HAS_ROLE = "3002——账户已经拥有角色";
    public static final String ROLE_NAME_HAS_EXIST = "3003——角色名字已存在";
    public static final String MAIN_ROOM_DRIEND_DELETE_ROLE_NOT_EXIST = "3004——删除的好友不存在";
}