package thread;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * 信号量的学习。
* @ClassName: BoundedHashSet 
* @Description: TODO 
* @Author 仇文敏
* @Date 2018年3月12日 下午1:21:40 
* @Modify  
* @CopyRight 杭州淘艺数据技术有限公司
* @param <T>
 */
public class BoundedHashSet<T> {
	
	private final Set<T> set;
	private final Semaphore sem;
	
	public BoundedHashSet(int bound){
		this.set = Collections.synchronizedSet(new HashSet<T>());
		sem = new Semaphore(bound);
	}
	
	public boolean add(T o) throws InterruptedException{
		sem.acquire();
		boolean wasAdded = false;
		try {
			wasAdded = set.add(o);
			return wasAdded;
		} finally{
			if(!wasAdded)
				sem.release();
		}
	}
	
	public boolean remove(Object o){
		boolean wasRemoved = set.remove(o);
		if(wasRemoved)
			sem.release();
		return wasRemoved;
	}

}
