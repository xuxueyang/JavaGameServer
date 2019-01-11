
import java.util.Stack;

class UserTokenPool
{
    private Stack<UserToken> pool;
    public UserTokenPool(int max)
    {
        pool = new Stack<UserToken>();
    }

    public UserToken pop()
    {
        return pool.pop();
    }
    public void push(UserToken token)
    {
        if (token != null)
        {
            pool.push(token);
        }
    }
    private int size;

    public int getSize() {
        return pool.size();
    }
}