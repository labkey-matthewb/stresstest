package stresstest;
import java.util.concurrent.Callable;

public class Stats
{
    final String name;
    int count = 0;
    double total = 0.0;
    double min = Double.MAX_VALUE;
    double max = -Double.MAX_VALUE;

    public Stats(String name)
    {
        this.name = name;
    }
    public void update(double d)
    {
        count++;
        total += d;
        min = Math.min(min,d);
        max = Math.max(max,d);
    }
    public String toString()
    {
        if (count ==  0)
            return name + "\tcount=0";
        return name + ": count=" + count + " avg=" + Math.round(total/count) + " min=" + min + " max=" + max;
    }
    public <V> V time(Callable<V> call) throws Exception
    {
        long start = System.currentTimeMillis();
        V ret = call.call();
        update(System.currentTimeMillis()-start);
        return ret;
    }
}
