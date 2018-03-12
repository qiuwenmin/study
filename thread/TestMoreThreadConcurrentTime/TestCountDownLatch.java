package thread;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;
/**
 * 
* @ClassName: TestCountDownLatch 
* @Description: TODO 
* @Author 仇文敏
* @Date 2018年3月12日 上午10:01:54 
* @Modify  
* @CopyRight 测试多个线程同时执行需要的时间，当第一个线程进来时等待，防止第一个线程先执行造成误差
 */
public class TestCountDownLatch{
	public long timeTask(int nThread,final Runnable task){
		final CountDownLatch startGate = new CountDownLatch(1);
		final CountDownLatch endGate = new CountDownLatch(nThread);
		
		for (int i = 0; i < nThread; i++) {
			Thread t = new Thread(){
				public void run(){
					try {
						startGate.await();
						try{
							task.run();
						}finally{
							endGate.countDown();
						}
					} catch (InterruptedException e) {
						// TODO: handle exception
					}
				}
			};
			t.start();
		}
		long start = System.nanoTime();
		startGate.countDown();
		try {
			endGate.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long end = System.nanoTime();
		return end - start;
		
	}
	
	@Test
	public void test(){
		TestCountDownLatch latch = new TestCountDownLatch();
		//MyThread需要测试的线程
		Thread thread = new MyThread("Test thread");
		System.err.println(latch.timeTask(10, thread));
	}
}
