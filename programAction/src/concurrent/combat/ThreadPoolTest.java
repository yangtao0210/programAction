package concurrent.combat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Author:guaika
 * Description:线程池异常捕获测试
 */
public class ThreadPoolTest {
    public static void main(String[] args) {

        //缓存线程池测试
        ExecutorService executorService = Executors.newCachedThreadPool();
        for(int i = 0; i < 5; i++){
            executorService.execute(()->{
                System.out.println("cachedThreadPool---->"+Thread.currentThread().getName()+"正在执行");
            });
        }


        //单个线程的线程池测试
        ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
        for(int i = 0; i < 5; i++){
            singleThreadPool.execute(()->{
                System.out.println("singleThreadPool------>"+Thread.currentThread().getName()+"正在执行");
            });
        }

        //定时或者周期执行线程的线程池
        ScheduledExecutorService scheduleThreadPool = Executors.newScheduledThreadPool(1);

        //固定延时执行任务
       scheduleThreadPool.scheduleWithFixedDelay(()->{
           System.out.println("currentTime"+System.currentTimeMillis());
           System.out.println("schedule---->"+Thread.currentThread().getName()+"正在执行");
       },1,3,TimeUnit.SECONDS);


       //周期执行任务
        scheduleThreadPool.scheduleAtFixedRate(()->{
            System.out.println("current Time:"+System.currentTimeMillis());
            System.out.println("schedule---atFixedRate---->"+Thread.currentThread().getName()+"正在执行");
        },1,3,TimeUnit.SECONDS);

        //固定线程数目的线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(10);

        for(int i = 0; i < Integer.MAX_VALUE; i++){
            threadPool.execute(()->{
                //Runnable command
                try{
                    Thread.sleep(10000);
                }catch (InterruptedException e){
                    System.out.println("出现异常");
                }
            });
        }
        //try...catch()处理异常
        for(int i = 0; i < 5; i++){
            threadPool.submit(()->{
                System.out.println("current thread name:"+Thread.currentThread().getName());
                try{
                    Object object = null;
                    System.out.println("result"+object.toString());
                }
                catch (Exception e){
                    System.out.println("程序出异常了！");
                }
            });
        }

        //Future.get方法处理异常
        for(int i = 0; i < 5; i++){
            Future future = threadPool.submit(()->{
                System.out.println("current thread name:"+Thread.currentThread().getName());
                Object object = null;
                System.out.println("result"+object.toString());
            });
            try{
                future.get();
            }
            catch (Exception e){
                System.out.println("程序出异常了！");
            }
        }

        uncaughtExceptionHandler();



    }

    //uncaughtExceptionHandler处理异常
    public static void uncaughtExceptionHandler(){
        ExecutorService threadPool = Executors.newFixedThreadPool(1,r -> {
            Thread t = new Thread(r);
            t.setUncaughtExceptionHandler((t1,e)->{
                System.out.println(t1.getName()+"线程抛出的异常"+e);
            });
            return t;
        });
        //线程池执行任务的方法
        threadPool.execute(()->{
            Map<String,Object> objectMap = new HashMap<>();
            objectMap.put("o1",new Integer(5));
            objectMap.put("o2",null);
            objectMap.put("o3",new Integer(3));
            objectMap.put("o3",new Integer(5));
            Iterator<Map.Entry<String, Object>> iterator = objectMap.entrySet().iterator();
            while(iterator.hasNext()){
                System.out.println("result-->"+objectMap.get(iterator.next().getKey()).toString());
            }

        });
    }

}

//重写ThreadPoolExecutor的afterExecute方法，处理传递的异常引用
class ExtendExecutor extends ThreadPoolExecutor{

    public ExtendExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if(t == null && r instanceof Future<?>){
            try{
                Object result = ((Future<?>) r).get();
            }catch (CancellationException ce){
                t = ce;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                t = e.getCause();
            }
        }
        if(t != null){
            System.out.println(t);
        }
    }
}
