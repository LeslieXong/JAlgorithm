package particlefilter;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import particlefilter.Utils.GeoPoint;

public class JUnittest
{

	@Test
	public void test_dnorm()
	{
		double dx = 0.01;
		double x = -1;
		double integral = 0.0;
		while (x <= 1)
		{
			integral += Utils.dnorm(x, 0, 1) + Utils.dnorm(x + dx, 0, 1);
			x += dx;
		}
		integral *= 0.5 * dx;
		assertEquals(0.682, integral, 0.01);
	}

	@Test
	public void test_sampligwheel()
	{
		double SQRTTWOPI = 2.50662827463;
		List<Double> weight = new ArrayList<Double>();//proportional to Gaussian distribution with mu=0, std=1
		List<Double> prior = new ArrayList<Double>();//uniformly distributed in [0,4]
		int N = 100000;//number of particles
		Random r = new Random();
		double rn = 0.0;

		for (int i = 0; i < N; i++)
		{
			rn = r.nextDouble() * 4;
			weight.add(1 / SQRTTWOPI * Math.exp(-rn * rn / 2));
			prior.add(rn);
		}
		List<Integer> resampled = SamplingWheel.Sample(weight);
		int count = 0;
		//count number of resampled particles with states < 1
		for (int i = 0; i < N; i++)
		{
			if (Math.abs(prior.get(resampled.get(i))) < 1)
			{
				count += 1;
			}

		}
		assertEquals(0.682, (double) count / N, 0.01);
	}

	@Test
	public void test_to_rad()
	{
		double rad = Utils.to_rad(270);
		assertEquals(1.5 * (Math.PI), rad, 0.000001);
	}

	@Test
	public void test_to_decimal_degrees()
	{
		assertEquals(41.88194444444444, Utils.to_decimal_degrees(41, 52, 55), 0.00000001);
		assertEquals(-87.62777777777777, -Utils.to_decimal_degrees(87, 37, 40), 0.00000001);
	}

	@Test
	public void test_move_lat_lon1()
	{
		//Chicago downtown
		Utils.GeoPoint pt = new Utils.GeoPoint(Utils.to_decimal_degrees(41, 52, 55),
				-Utils.to_decimal_degrees(87, 37, 40));
		//move lat by 1 second north
		Utils.GeoPoint pt1 = new Utils.GeoPoint(Utils.to_decimal_degrees(41, 52, 56),
				-Utils.to_decimal_degrees(87, 37, 40));
		//Length of a Degree of Latitude and Longitude calculator:
		//http://msi.nga.mil/MSISiteContent/StaticFiles/Calculators/degree.html
		Utils.move_lat_lon(pt, 30.8530451453, 0);
		//based on the assumption that there are 30.8530444444 meters in 1 second
		assertEquals((pt1.lat - pt.lat) * 3600 * 30.8530444444 * 100, 0, 3);//3 cm
		assertEquals((pt1.lon - pt.lon) * 3600 * 23.0566666667 * 100, 0, 0.1);//0.1 cm
	}

	@Test
	public void test_move_lat_lon2()
	{
		//Chicago downtown
		Utils.GeoPoint pt = new Utils.GeoPoint(Utils.to_decimal_degrees(41, 52, 55),
				-Utils.to_decimal_degrees(87, 37, 40));
		//move lat by 10 seconds north
		Utils.GeoPoint pt1 = new Utils.GeoPoint(Utils.to_decimal_degrees(41, 52, 65),
				-Utils.to_decimal_degrees(87, 37, 40));
		Utils.move_lat_lon(pt, 10 * 30.8530451453, 0);
		//based on the assumption that there are 30.8530444444 meters in 1 second of latitude
		assertEquals((pt1.lat - pt.lat) * 3600 * 30.8530444444 * 100, 0, 25);//25 cm
		assertEquals((pt1.lon - pt.lon) * 3600 * 23.0566666667 * 100, 0, 0.1);// 0.1 cm
	}

	@Test
	public void test_move_lat_lon3()
	{
		//Chicago downtown
		Utils.GeoPoint pt = new Utils.GeoPoint(Utils.to_decimal_degrees(41, 52, 55),
				-Utils.to_decimal_degrees(87, 37, 40));
		//move lon by 1 sec west
		Utils.GeoPoint pt1 = new Utils.GeoPoint(Utils.to_decimal_degrees(41, 52, 55),
				-Utils.to_decimal_degrees(87, 37, 41));
		//Length of a Degree of Latitude and Longitude calculator:
		//http://msi.nga.mil/MSISiteContent/StaticFiles/Calculators/degree.html
		Utils.move_lat_lon(pt, 23.0566666667, 270);
		//based on the assumption that there are 23.0566666667 meters in 1 second of longitude
		assertEquals((pt1.lat - pt.lat) * 3600 * 30.8530444444 * 100, 0, 0.1);
		assertEquals((pt1.lon - pt.lon) * 3600 * 23.0566666667 * 100, 0, 7);//7 cm
	}

	@Test
	public void test_move_lat_lon30()
	{
		//Chicago downtown
		Utils.GeoPoint pt = new Utils.GeoPoint(Utils.to_decimal_degrees(41, 52, 55),
				-Utils.to_decimal_degrees(87, 37, 40));
		//move lon by 1 sec east
		Utils.GeoPoint pt1 = new Utils.GeoPoint(Utils.to_decimal_degrees(41, 52, 55),
				-Utils.to_decimal_degrees(87, 37, 30));
		//Length of a Degree of Latitude and Longitude calculator:
		//http://msi.nga.mil/MSISiteContent/StaticFiles/Calculators/degree.html
		double dist = 10 * 23.0566666667;
		Utils.move_lat_lon(pt, dist, 90);
		//based on the assumption that there are 23.0566666667 meters in 1 second of longitude
		assertEquals((pt1.lat - pt.lat) * 3600 * 30.8530444444 * 100, 0, 0.4);
		assertEquals((pt1.lon - pt.lon) * 3600 * 23.0566666667 * 100, 0, 70);//70 cm
	}

	@Test
	public void test_move_lat_lon4()
	{
		//Chicago downtown
		Utils.GeoPoint pt = new Utils.GeoPoint(Utils.to_decimal_degrees(41, 52, 55),
				-Utils.to_decimal_degrees(87, 37, 40));
		//move lon by 1 sec north and 1 sec west
		Utils.GeoPoint pt1 = new Utils.GeoPoint(Utils.to_decimal_degrees(41, 52, 56),
				-Utils.to_decimal_degrees(87, 37, 41));
		//Length of a Degree of Latitude and Longitude calculator:
		//http://msi.nga.mil/MSISiteContent/StaticFiles/Calculators/degree.html
		double dist = Math.sqrt(23.0566666667 * 23.0566666667 + 30.8530451453 * 30.8530451453);//38.5 m
		//		dist = Utils.distance(pt.lat, pt.lon, pt1.lat, pt1.lon);
		Utils.move_lat_lon(pt, dist, 323.31);
		//based on the assumption that there are 23.0566666667 meters in 1 second of longitude
		assertEquals((pt1.lat - pt.lat) * 3600 * 30.8530444444 * 100, 0, 3);
		assertEquals((pt1.lon - pt.lon) * 3600 * 23.0566666667 * 100, 0, 3);
	}

	@Test
	public void test_distance()
	{
		Utils.GeoPoint pt = new Utils.GeoPoint(Utils.to_decimal_degrees(41, 52, 55),
				-Utils.to_decimal_degrees(87, 37, 40));
		Utils.GeoPoint pt1 = new Utils.GeoPoint(Utils.to_decimal_degrees(41, 52, 56),
				-Utils.to_decimal_degrees(87, 37, 41));
		double dist = Utils.distance(pt.lat, pt.lon, pt1.lat, pt1.lon);
		assertEquals(dist, 38.51, 0.1);//10cm
	}

	@Test
	public void test_bearing()
	{
		Utils.GeoPoint pt = new Utils.GeoPoint(Utils.to_decimal_degrees(41, 52, 55),
				-Utils.to_decimal_degrees(87, 37, 40));
		Utils.GeoPoint pt1 = new Utils.GeoPoint(Utils.to_decimal_degrees(41, 52, 56),
				-Utils.to_decimal_degrees(87, 37, 41));
		double dist = Utils.bearing(pt.lat, pt.lon, pt1.lat, pt1.lon);
		assertEquals(323.31, dist, 0.1);
	}

	@Test
	public void test_proj_to_world_mercator()
	{
		GeoPoint pt1 = Utils.proj_to_world_mercator(Utils.to_decimal_degrees(41, 52, 55),
				-Utils.to_decimal_degrees(87, 37, 40));
		GeoPoint pt2 = Utils.proj_to_world_mercator(Utils.to_decimal_degrees(41, 52, 55),
				-Utils.to_decimal_degrees(87, 37, 41));
		double dist = Math.sqrt(Math.pow(pt1.lat - pt2.lat, 2) + Math.pow(pt1.lon - pt2.lon, 2));
		assertEquals(dist, 30.8530444444, 0.1);//10cm
	}

	@Test
	public void test_proj_to_world_mercator1()
	{
		GeoPoint pt1 = Utils.proj_to_world_mercator(Utils.to_decimal_degrees(41, 52, 55),
				-Utils.to_decimal_degrees(87, 37, 40));
		GeoPoint pt2 = Utils.proj_to_world_mercator(Utils.to_decimal_degrees(41, 52, 56),
				-Utils.to_decimal_degrees(87, 37, 40));
		double dist = Math.sqrt(Math.pow(pt1.lat - pt2.lat, 2) + Math.pow(pt1.lon - pt2.lon, 2));
		assertEquals(dist, 30.8530444444, 0.1);//10cm
	}

}
