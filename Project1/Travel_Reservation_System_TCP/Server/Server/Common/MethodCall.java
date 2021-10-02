package Server.Common;

import java.util.ArrayList;
import java.util.List;

public class MethodCall {

    public String method;
    public List<Object> args;

    public Object[] getArgList(Class<?>[] paramTypes)
    {
        List<Object> list = new ArrayList<>();
        for (int i=0; i<paramTypes.length; i++)
        {
            Class cls = paramTypes[i];
            list.add(cls.cast(this.args.get(i)));
        }
        return list.toArray();
    }
}
