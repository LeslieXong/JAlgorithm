package simulator;

import java.awt.Color;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.List;
import java.awt.Point;

import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Random;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTextArea;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeListener;
import particlefilter.FileWrite;
import particlefilter.Particle;
import particlefilter.ParticleFilter;
import util.Matrix;
import util.Point2D;
import util.Point2D;

import javax.swing.event.ChangeEvent;

import kalmanfilter.*;

/**
 * Encouraged by https://github.com/erhs-53-hackers/Particle-Filter/tree/master/src/particlefilter
 * 
 * @author LeslieXong
 *
 */
public class Simulate extends JFrame
{
	private static final long serialVersionUID = 1L;

	private ParticleFilter particleFilter;
	private Particle insEstimator;
	private Random random;

	private KalmanFilter kalmanFilter;

	private static final Point2D[] landmarks = new Point2D[] { new Point2D(10.5, 16.0f), new Point2D(62.5, 42.0f),
			new Point2D(22.5f, 40.0f), new Point2D(39.9f, 25.0f),
			new Point2D(63.0, 14.0f), new Point2D(33.7f, 4.2f) };

	final int PARTICLES_NUM = 2000;
	final float WORLD_WIDTH = 75f, WORLD_HEIGHT = 50f;
	final int G_MARGIN_X = 12, G_MARGIN_Y = 35;
	final float SCALE = 10f;  //pixel per meter
	int drawWidth;
	int drawHeight;

	private Image image;
	private Graphics2D graphics;

	private JPanel contentPane;
	private JTextField textFieldMoveNoise;
	private JTextField textFieldTurnNoise;
	private JTextField textFieldSenseNoise;
	private JTextArea txtNotice;

	//default display
	private boolean showKF = true;
	private boolean showPF = true;

	private boolean showMLE = true;
	private boolean showINS = true;
	private boolean showParticles = true;

	private float moveNoise = 1;
	private float orientNoise = 8;
	private float senseNoise = 3;

	private LinkedList<Point2D> truePosition = new LinkedList<Point2D>();
	private LinkedList<Point2D> kfPosition = new LinkedList<Point2D>();
	private LinkedList<Point2D> pfPosition = new LinkedList<Point2D>();
	private LinkedList<Point2D> mlePosition = new LinkedList<Point2D>();
	private LinkedList<Point2D> insPosition = new LinkedList<Point2D>();

	private float insError = 0;
	private float pfError = 0;
	private float kfError = 0;
	private float mleError = 0;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					Simulate frame = new Simulate();
					frame.setVisible(true);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	private void setUp()
	{
		insError = 0;
		pfError = 0;
		kfError = 0;
		mleError = 0;
		random = new Random();
		kalmanFilter = null;

		truePosition = new LinkedList<Point2D>();
		kfPosition = new LinkedList<Point2D>();
		pfPosition = new LinkedList<Point2D>();
		mlePosition = new LinkedList<Point2D>();
		insPosition = new LinkedList<>();

		insEstimator = new Particle(landmarks, WORLD_WIDTH, WORLD_HEIGHT);
		insEstimator.setSenseNoise(senseNoise);

		particleFilter = new ParticleFilter(PARTICLES_NUM, landmarks, WORLD_WIDTH, WORLD_HEIGHT);
		particleFilter.setSenseNoise(senseNoise);
		
		drawWidth = (int) (WORLD_WIDTH * SCALE);
		drawHeight = (int) (WORLD_HEIGHT * SCALE);
		image = new BufferedImage(drawWidth, (int) drawHeight, BufferedImage.BITMASK);
		graphics = (Graphics2D) image.getGraphics();
		graphics.drawRect(0, 0, drawWidth - 1, drawHeight - 1);
	}

	@Override
	public void update(Graphics g)
	{
		paint(g);
	}

	private void drawPoints(boolean ifdraw, Color color, Object[] points, int r)
	{
		if (ifdraw)
		{
			graphics.setPaint(color);
			for (Object o : points)
			{
				Point2D p = (Point2D) o;
				graphics.fillOval((int) (p.x * SCALE) - r / 2, (int) (p.y * SCALE) - r / 2, r, r);
			}
		}
	}

	@Override
	public void paint(Graphics g)
	{
		super.paint(g);

		/* Draw simulation environment ****************************************/
		graphics.clearRect(0, 0, drawWidth, drawHeight);
		graphics.setPaint(Color.white);
		graphics.drawRect(0, 0, drawWidth - 1, drawHeight - 1);

		/* Draw all landmarks *************************************************/
		drawPoints(true, Color.CYAN, landmarks, 12);

		/* Draw the true trajectory ********************************************/
		drawPoints(true, Color.RED, truePosition.toArray(), 15);

		/* Draw all particles *************************************************/
		int size = particleFilter.particles.length;
		Point2D[] particles = new Point2D[size];
		for (int i = 0; i < size; i++)
		{
			particles[i] = new Point2D(particleFilter.particles[i].x, particleFilter.particles[i].y);
		}
		drawPoints(showParticles, Color.PINK, particles, 2);

		drawPoints(showMLE, Color.YELLOW, mlePosition.toArray(), 10);
		drawPoints(showKF, Color.GREEN, kfPosition.toArray(), 10);
		drawPoints(showPF, Color.BLUE, pfPosition.toArray(), 10);
		drawPoints(showINS, Color.magenta, insPosition.toArray(), 10);

		/* Draw current position info ********************************************/
		if (truePosition.size() > 0)
		{
			graphics.setPaint(Color.red);
			graphics.drawString(String.format("X( Width: %.1fm)  %.1f", WORLD_WIDTH, truePosition.getLast().x), 10, 20);
			graphics.drawString(String.format("Y( Height: %.1fm) %.1f", WORLD_HEIGHT, truePosition.getLast().y), 10,
					40);
			txtNotice.setText(
					String.format("MLE:%.2fm\nINS:%.2fm \nKF:%.2fm\nPF:%.2fm\n", mleError, insError, kfError, pfError));
		}

		g.drawImage(image, G_MARGIN_X, G_MARGIN_Y, rootPane);
	}

	private void saveResult()
	{
		txtNotice.setText("Saving...");
		String file = "src/pfGUI/simulate-out.csv";
		FileWrite fw = new FileWrite(file);
		String c = String.format("True,%.2f,MLE,%.2f,KF,%.2f,PF,%.2f,INS,%.2f\n", 0f, mleError, kfError, pfError,
				insError);
		fw.write(c);
		for (int i = 0; i < truePosition.size(); i++)
		{
			String s = String.format("%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f\n",
					truePosition.get(i).x, truePosition.get(i).y, mlePosition.get(i).x,
					mlePosition.get(i).y, kfPosition.get(i).x, kfPosition.get(i).y, pfPosition.get(i).x,
					pfPosition.get(i).y, insPosition.get(i).x, insPosition.get(i).y);
			fw.write(s);
		}

		fw.closeStream();
		txtNotice.setText("data saved in:\n" + file);
	}

	private void updateNoise()
	{
		moveNoise = Float.parseFloat(textFieldMoveNoise.getText());
		orientNoise = (float) Math.toRadians(Double.parseDouble(textFieldTurnNoise.getText()));
		senseNoise = Float.parseFloat(textFieldSenseNoise.getText());

		particleFilter.setSenseNoise(senseNoise);
		insEstimator.setSenseNoise(senseNoise);
	}

	/**
	 * Use distance measurement to estimate the position
	 * 
	 * @param Z
	 * @return
	 */
	private Point2D getDisAloneEstimation(float[] Z)
	{
		ParticleFilter mleEstimator = new ParticleFilter(PARTICLES_NUM, landmarks, WORLD_WIDTH, WORLD_HEIGHT);
		mleEstimator.setSenseNoise(senseNoise);

		try
		{
			return mleEstimator.getEapPosition(Z,1);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Create the frame.
	 */
	public Simulate()
	{
		setUp();
		setTitle("Particle/Kalman filter Simulator(By lesliexong@qq.com)");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1000, 550);
		contentPane = new JPanel();

		//choose a new position
		contentPane.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				updateNoise();

				float x = e.getX() / SCALE;
				float y = e.getY() / SCALE;

				try
				{
					if (truePosition.size() != 0)
					{
						float deltax = x - truePosition.getLast().x;
						float deltay = y - truePosition.getLast().y;

						//simulate the move sensing observing
						float foward = (float) Math.sqrt(deltax * deltax + deltay * deltay)
								+ (float) random.nextGaussian() * moveNoise;  //may <0?
						float direction = (float) Math.atan2(deltay, deltax)
								+ (float) random.nextGaussian() * orientNoise + 0.1f; // system error,radians?

						insEstimator.move(direction, foward);
						insError += (Utils.distance(insEstimator.x, insEstimator.y, x, y) - insError)
								/ (truePosition.size() + 1);
						insPosition.add(new Point2D(insEstimator.x, insEstimator.y));

						double[][] pdr = { { foward * Math.cos(direction), 0 }, { 0, foward * Math.cos(direction) } };
						kalmanFilter.setStateTransitModelF(new Matrix(pdr));
						kalmanFilter.setProcessNoiseCovQ(moveNoise * 1.3f);  //Set transit Noise include angle error

						particleFilter.move(direction, foward);
					} else
					{
						insEstimator.set(x, y, 0, 0);  //initial
						insPosition.add(new Point2D(x, y));
					}
					truePosition.add(new Point2D(x, y));

					float[] Z = Utils.simulateSense(landmarks, senseNoise, new Point2D(x, y));

					Point2D pos = particleFilter.getEapPosition(Z,0);
					particleFilter.reSample2();
					pfPosition.add(new Point2D(pos.x, pos.y));
					pfError += (Utils.distance(pos.x, pos.y, x, y) - pfError) / truePosition.size();

					pos = getDisAloneEstimation(Z);
					mlePosition.add(new Point2D(pos.x, pos.y));
					mleError += (Utils.distance(pos.x, pos.y, x, y) - mleError) / truePosition.size();

					double[] mlePos = { pos.x, pos.y };
					Matrix zMatrix = new Matrix(mlePos).trans();

					if (kalmanFilter == null)
					{
						kalmanFilter = new KalmanFilter(2, 2);
						kalmanFilter.setCurrentState(zMatrix, new Matrix(2, senseNoise * 0.5));
					} else
					{
						kalmanFilter.setObsvNoiseCovR(senseNoise * 0.5);
						Matrix state = kalmanFilter.filter(zMatrix);
						pos = new Point2D(state.value(0, 0), state.value(1, 0));
						kfPosition.add(pos);
						kfError += (Utils.distance(pos.x, pos.y, x, y) - kfError) / truePosition.size();
					}
				} catch (Exception ex)
				{
					System.out.println(ex.getMessage());
				}
				Simulate.this.repaint();
			}
		});

		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JLabel lblNoiseSet = new JLabel("Noise std set:");

		JLabel lblMoveNiose = new JLabel("Move Noise(meter)");
		textFieldMoveNoise = new JTextField();
		textFieldMoveNoise.setToolTipText("Meters");
		textFieldMoveNoise.setText(String.valueOf(moveNoise));
		textFieldMoveNoise.setColumns(10);

		JLabel lblOrientNoise = new JLabel("Orient Noise(degree)");
		textFieldTurnNoise = new JTextField();
		textFieldTurnNoise.setToolTipText("Degree");
		textFieldTurnNoise.setText(String.valueOf(orientNoise));
		textFieldTurnNoise.setColumns(10);

		JLabel lblSenseNoise = new JLabel("Sense Noise(meter)");
		textFieldSenseNoise = new JTextField();
		textFieldSenseNoise.setToolTipText("Meters");
		textFieldSenseNoise.setText(String.valueOf(senseNoise));
		textFieldSenseNoise.setColumns(10);

		JButton btnSave = new JButton("Save Result");
		btnSave.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				saveResult();
			}
		});

		JButton btnReset = new JButton("Reset");
		btnReset.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setUp();
				Simulate.this.repaint();
			}
		});

		JLabel lblShow = new JLabel("Display set:");

		final JCheckBox chckbxKF = new JCheckBox("Kalman Filter");
		chckbxKF.setBackground(Color.GREEN);
		chckbxKF.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				showKF = chckbxKF.isSelected();
				Simulate.this.repaint();
			}
		});
		chckbxKF.setSelected(showKF);

		final JCheckBox chckbxPF = new JCheckBox("Particle Filter");
		chckbxPF.setForeground(Color.WHITE);
		chckbxPF.setBackground(Color.BLUE);
		chckbxPF.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				showPF = chckbxPF.isSelected();
				Simulate.this.repaint();
			}
		});
		chckbxPF.setSelected(showPF);

		final JCheckBox chckbxMle = new JCheckBox("MLE Alone(triangle)");
		chckbxMle.setBackground(Color.YELLOW);
		chckbxMle.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				showMLE = chckbxMle.isSelected();
				Simulate.this.repaint();
			}
		});
		chckbxMle.setSelected(showMLE);

		final JCheckBox chckbxParticles = new JCheckBox("Particles");
		chckbxParticles.setBackground(Color.PINK);
		chckbxParticles.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				showParticles = chckbxParticles.isSelected();
				Simulate.this.repaint();
			}
		});
		chckbxParticles.setSelected(showParticles);

		txtNotice = new JTextArea();
		txtNotice.setText(
				"Notice:\n   Click on graphic step by step to \n create a trajectory for simulating \n tracking.");

		final JCheckBox chckbxIns = new JCheckBox("INS Alone");
		chckbxIns.setBackground(Color.MAGENTA);
		chckbxIns.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				showINS = chckbxIns.isSelected();
				Simulate.this.repaint();
			}
		});
		chckbxIns.setSelected(showINS);

		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
				gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_contentPane.createSequentialGroup()
								.addContainerGap(774, Short.MAX_VALUE)
								.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
										.addComponent(txtNotice, GroupLayout.PREFERRED_SIZE, 180,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(lblShow)
										.addComponent(chckbxKF)
										.addComponent(chckbxPF)
										.addComponent(chckbxIns)
										.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
												.addGroup(gl_contentPane.createSequentialGroup()
														.addGroup(gl_contentPane
																.createParallelGroup(Alignment.TRAILING, false)
																.addComponent(btnReset, Alignment.LEADING,
																		GroupLayout.DEFAULT_SIZE,
																		GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																.addComponent(btnSave, Alignment.LEADING,
																		GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE))
														.addGap(75))
												.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
														.addGroup(gl_contentPane.createSequentialGroup()
																.addGroup(gl_contentPane
																		.createParallelGroup(Alignment.TRAILING, false)
																		.addComponent(lblSenseNoise, Alignment.LEADING,
																				GroupLayout.DEFAULT_SIZE,
																				GroupLayout.DEFAULT_SIZE,
																				Short.MAX_VALUE)
																		.addComponent(lblMoveNiose, Alignment.LEADING,
																				GroupLayout.DEFAULT_SIZE,
																				GroupLayout.DEFAULT_SIZE,
																				Short.MAX_VALUE)
																		.addComponent(lblOrientNoise,
																				Alignment.LEADING))
																.addGap(33)
																.addGroup(gl_contentPane
																		.createParallelGroup(Alignment.LEADING, false)
																		.addComponent(textFieldSenseNoise, 0, 0,
																				Short.MAX_VALUE)
																		.addComponent(textFieldTurnNoise,
																				GroupLayout.PREFERRED_SIZE, 37,
																				GroupLayout.PREFERRED_SIZE)))
														.addComponent(textFieldMoveNoise, GroupLayout.PREFERRED_SIZE,
																37, GroupLayout.PREFERRED_SIZE)
														.addComponent(lblNoiseSet, Alignment.LEADING)))
										.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING, false)
												.addComponent(chckbxParticles, Alignment.LEADING, 0, 0, Short.MAX_VALUE)
												.addComponent(chckbxMle, Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
														GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
								.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		gl_contentPane.setVerticalGroup(
				gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
								.addComponent(lblNoiseSet)
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
										.addComponent(textFieldMoveNoise, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(lblMoveNiose))
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
										.addComponent(textFieldTurnNoise, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(lblOrientNoise))
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
										.addComponent(textFieldSenseNoise, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(lblSenseNoise))
								.addGap(33)
								.addComponent(lblShow)
								.addPreferredGap(ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
								.addComponent(chckbxIns)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(chckbxKF)
								.addGap(1)
								.addComponent(chckbxPF)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(chckbxParticles)
								.addGap(2)
								.addComponent(chckbxMle)
								.addGap(26)
								.addComponent(btnSave)
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addComponent(btnReset)
								.addGap(18)
								.addComponent(txtNotice, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
								.addGap(42)));
		contentPane.setLayout(gl_contentPane);
	}
}
