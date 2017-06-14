package pedometer;

/**
 * 走步检测器，用于检测走步并计数
 */
public class StepDetector
{
	public static int CURRENT_STEP = 0;
	public static float SENSITIVITY = 0;

	float vl = 0;// 低通滤波缓存

	private float lastValue = 0;
	private float lastDirection = 0;
	private float Extremes[] = new float[2];
	private float lastDiff = 0;
	private int lastMatch = -1;

	/**
	 * 传入上下文的构造函数
	 * 
	 * @param context
	 */
	public StepDetector(float sen)
	{
		SENSITIVITY = sen;

		vl = 0;
	}

	/**
	 * 脚步探测 一步则返回true
	 * 
	 * @param values
	 * @return
	 */
	public boolean stepJudge(float v)
	{
		{
			// 加速度大于上一个 dir=1(波谷), 小于取-1（波峰），等于取0
			float direction = (v > lastValue ? 1 : (v < lastValue ? -1 : 0));

			// 峰或者谷
			if (direction == -lastDirection)
			{
				// 0是波谷，1是波峰 
				int extType = (direction > 0 ? 0 : 1);// minumum or maximum?

				Extremes[extType] = lastValue;//mLastExtemes[0]表示波谷值，[1]表示波峰值

				// mLastValues是波峰谷值 //差分阈值检测？
				float diff = Math.abs(Extremes[extType] - Extremes[1 - extType]);
				
				if (diff > SENSITIVITY)
				{
					boolean isAlmostAsLargeAsPrevious = diff > (lastDiff * 1 / 3);
					boolean isPreviousLargeEnough = lastDiff > (diff / 3);// 保证是一个波的两边比较均匀
					boolean isNotContra = (lastMatch != 1 - extType);// 和上次相同

					if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra)
					{
						CURRENT_STEP++;
						lastMatch = extType;
						
						lastDiff = diff;
						lastDirection = direction;
						lastValue = v;
						System.out.println(v);
						return true;
					} else
					{
						//lastMatch = -1;
					}
				}
				lastDiff = diff;
			}

			System.out.println(0);
			lastDirection = direction;
			lastValue = v;
			return false;
		}
	}

	// 和一阶卡尔曼滤波比较像
	private float lowPassFilter(float v)
	{
		if (vl == 0)
		{
			vl = v;
		} else
		{
			vl = vl * 0.8f + v * 0.2f;
		}
		return vl;
	}

	public void clearBuffer()
	{
		lastValue = 0;
		lastDirection = 0;

		lastDiff = 0;
		lastMatch = -1;
		vl = 0;
	}
}
