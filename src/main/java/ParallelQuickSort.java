import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/*
* 利用ForkJoinPool实现了快速排序的并行实现。
* 并且和不利用并行的原始快速排序进行了速度上的对比，
* 发现：
*   在小数据的情况下，不使用并行的快速排序性能更好。
*   但是大数据的情况下，并行的快速排序明显更优。
* 这是由于数据小的时候，并行化带来的线程调度、任务拆分开销远大于排序本身耗时。
* */
public class ParallelQuickSort {
    public static void main(String[] args) {
        ForkJoinPool pool = new ForkJoinPool();
        Random random = new Random();
        int size = 100000000;

        // 创建两个空的数组
        int[] array1 = new int[size];
        int[] array2 = new int[size];

        // 填充array1数组，并将array1的元素复制到array2
        for (int i = 0; i < size; i++) {
            int randomValue = random.nextInt();  // 生成一个随机整数
            array1[i] = randomValue;
            array2[i] = randomValue;  // 保证array2与array1相同
        }

        Task task = new Task(array1, 0, array1.length - 1);
        long start = System.nanoTime();
        pool.invoke(task);
        long duration = System.nanoTime() - start;
        System.out.println("耗时：" + duration / 1e6 + "ms");



        start=System.nanoTime();
        quickSort(array2,0, array2.length-1);
        duration=System.nanoTime()-start;
        System.out.println("耗时：" + duration / 1e6 + "ms");

    }
    public static int partition(int[] array,int left,int right){
        int pivot = array[left];
        while(left<right){
            while(left<right && array[right]>=pivot){
                right--;
            }
            array[left]=array[right];
            while(left<right && array[left]<=pivot){
                left++;
            }
            array[right]=array[left];
        }
        array[left]=pivot;
        return left;
    }
    public static void quickSort(int[] array,int left,int right){
        if(right<=left){
            return;
        }
        int mid = partition(array,left,right);
        quickSort(array,left,mid-1);
        quickSort(array,mid+1,right);
    }


    static class Task extends RecursiveAction {
        int[] array;
        int left;
        int right;

        public Task(int[] array,int left,int right){
            this.array=array;
            this.left=left;
            this.right=right;
        }
        public int partition(int[] array,int left,int right){
            int pivot = array[left];
            while(left<right){
                while(left<right && array[right]>=pivot){
                    right--;
                }
                array[left]=array[right];
                while(left<right && array[left]<=pivot){
                    left++;
                }
                array[right]=array[left];
            }
            array[left]=pivot;
            return left;
        }
        @Override
        protected void compute() {
            if(right<=left){
                return;
            }
            int mid = partition(array,left,right);
            Task leftTask = new Task(array,left,mid-1);
            Task rightTask = new Task(array,mid+1,right);
            invokeAll(leftTask,rightTask);
        }
    }
}
