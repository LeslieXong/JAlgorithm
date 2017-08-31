package pedometer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class pedometerTest
{
	public static void main(String[] args)
	{
		List<float[]> accData = readAccData("src//pedometer//acc.txt");
		List<Float> data = myDetector(accData);
		for (int i = 0; i < data.size(); i++)
		{
			//System.out.println(data.get(i));
		}
	}

	//测试计步器效果 比运用中少了时间窗口
	//延迟半步
	public static List<Float> myDetector(List<float[]> accData)
	{
		float vl = -10;
		List<Float> lowPassData = new LinkedList<Float>();
		for (int i = 0; i < accData.size(); i++)
		{
			float vSum = 0;
			for (int j = 0; j < 3; j++)
			{
				final float v = accData.get(i)[j];
				vSum += v;
			}

			if (vl == -10)
			{
				vl = vSum / 3f;
			}

			float v = vl * 0.8f + (vSum / 3f) * 0.2f;
			vl = v;
			lowPassData.add(v);
		}

		float peakBetweenValley = 0;
		float tempPeak = 0;
		List<Float> filterData = new LinkedList<Float>();

		float valleyLeft = 0;
		float valleyRight = 0;
		float MIN_FUSION = 0.6f;
		float MIN_STEP = 0.7f;

		for (int i = 1; i < (lowPassData.size() - 1); i++)
		{
			if (lowPassData.get(i) >= lowPassData.get(i - 1) && lowPassData.get(i) >= lowPassData.get(i + 1)) //this is a peak
			{
				float currentPeak = lowPassData.get(i);
				float diff1 = tempPeak - valleyRight;
				float diff2 = currentPeak - valleyRight;

				if (diff2 <= MIN_FUSION || diff1 <= MIN_FUSION) //该合并，当前被合并的波峰到tempPeak 
				{
					if (diff1 > diff2) //下行波峰，可能波谷可能继续下行，所以不换tempPeak
					{

					} else//上行波峰，可能波峰可能继续上行
					{
						tempPeak = currentPeak;
						peakBetweenValley = currentPeak;

						System.out.println(0 + " " + currentPeak);
						filterData.add(currentPeak);
					}

				} else//一个左右均衡的波谷，认为开始新的一步了，可以尝试计步 
				{
					if (peakBetweenValley != 0)
					{
						diff1 = peakBetweenValley - valleyLeft;
						diff2 = peakBetweenValley - valleyRight;

						if (diff1 >= MIN_STEP && diff2 >= MIN_STEP) //记一步
						{
							System.out.println(peakBetweenValley + " " + valleyRight);//代表波峰的那一步
							System.out.println(0 + " " + currentPeak);//代表接下来的波谷
						} else
						{
							System.out.println(0 + " " + valleyRight);//没计步的波峰波谷
							System.out.println(0 + "" + currentPeak);
						}
					}

					System.out.println(0+" "+currentPeak);
					filterData.add(valleyRight);
					filterData.add(currentPeak);

					valleyLeft = valleyRight;
					tempPeak = currentPeak;
					peakBetweenValley = currentPeak;
				}
			} else if (lowPassData.get(i) < lowPassData.get(i - 1) && lowPassData.get(i) < lowPassData.get(i + 1)) //this is a Valley
			{
				valleyRight = lowPassData.get(i);
				if (peakBetweenValley != 0)
				{
					float diff1 = peakBetweenValley - valleyLeft;
					float diff2 = peakBetweenValley - valleyRight;

					if (diff1 >= MIN_STEP && diff2 >= MIN_STEP) //记一步
					{
						System.out.println(peakBetweenValley + " " + valleyRight);
						//System.out.println(0 + " " + lowPassData.get(i));
						peakBetweenValley = 0;
					}
				}

			} else
			{
				continue;
			}
		}

		return filterData;
	}

	private static List<float[]> readAccData(String path)
	{
		List<float[]> data = new LinkedList<float[]>();

		try
		{
			File file = new File(path);

			if (!file.exists()) // 
			{
				System.out.println("wrong file path! ");
				return data;
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String temp = reader.readLine();
			while (true)
			{
				if (temp != null)
				{
					String[] a = temp.split(" ");
					float[] ac = { Float.parseFloat(a[0]), Float.parseFloat(a[1]), Float.parseFloat(a[2]) };
					data.add(ac);

					temp = reader.readLine();
				} else
				{
					reader.close();
					break;
				}
			}

			reader.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		return data;
	}

	}
