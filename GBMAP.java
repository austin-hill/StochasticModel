import java.util.*;
import java.lang.Math;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

class GBMAP {
	private double[] pastPrices;
	private ArrayList<ArrayList<Double>> predictedPrices = new ArrayList<>();
	private ArrayList<Double> predictedMean = new ArrayList<>();
	private double deltaT = (double)1/252;
	Random r = new Random();
	
	public GBMAP(double[] pastPrices) {
		this.pastPrices = pastPrices;
	}
	
	public int s(ArrayList<Double> predictedPricesI) {
		if (predictedPricesI == null) {
			return 0;
		} else {
			return predictedPricesI.size();
		}
	}
	
	public void initPredictedPrices(int n) {
		GBMAP[] S = new GBMAP[n];
		for (int j = 0; j < n; j++) {
			S[j] = new GBMAP(pastPrices);
			predictedPrices.add(new ArrayList<Double>());
		}
	}
	
	public void G(double t, int n, String currentStatsName, String currentPredictedPricesName, int startDateY, int startDateM, int startDateD, long numOfDates) {
		try {
			initPredictedPrices(n);
			
			PrintWriter p1 = new PrintWriter(new FileWriter(currentStatsName));
			p1.print(n + " %Number of samples\n" + startDateY + " %Year of earliest data\n" + startDateM + " %Month of earliest data\n" + startDateD + " %Day of earliest data\n" + numOfDates + " %Number of dates");
			p1.close();
			
			PrintWriter p = new PrintWriter(new FileWriter(currentPredictedPricesName));
			for (int i = 0; i < pastPrices.length; i++) {
				for (int j = 0; j < n + 1; j++) {
					p.print(Double.toString(pastPrices[i]) + " ");
				}
				p.println("");
			}
			for (int i = 0; i < t/deltaT - 0.5; i++) {
				double sum = 0;
				for (int j = 0; j < n; j++) {
					predictedPrices.get(j).add(sjp1(pastPrices.length + i, j));
					p.print(Double.toString(predictedPrices.get(j).get(i)) + " ");
					sum += predictedPrices.get(j).get(i);
				}
				predictedMean.add(sum / n);
				p.print(Double.toString(predictedMean.get(i)) + "\n");
			}
			p.close();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}
	
	public double returnI(int i, int k) {
		if (pastPrices.length > i) {
			return (pastPrices[i] - pastPrices[i-1]) / pastPrices[i-1];
		} else if (pastPrices.length == i && s(predictedPrices.get(k)) != 0) {
			return (predictedPrices.get(k).get(0) - pastPrices[i-1]) / pastPrices[i-1];
		} else if (pastPrices.length < i && s(predictedPrices.get(k)) > i - pastPrices.length) {
			return (predictedPrices.get(k).get(i - pastPrices.length) - predictedPrices.get(k).get(i - pastPrices.length - 1)) / predictedPrices.get(k).get(i - pastPrices.length - 1);
		} else {
			throw new smException("invalid index");
		}
	}
	
	public double returnRateJ(int j, int k) {
		int M = pastPrices.length + s(predictedPrices.get(k));
		double sum = 0;
		for (int i = 1; i < M; i++) {
			sum += returnI(i, k);
		}
		return sum / (deltaT * M);
	}
	
	public double volatilityJ(int j, int k) {
		int M = pastPrices.length + s(predictedPrices.get(k));
		double sum = 0;
		for (int i = 1; i < M; i++) {
			sum += returnI(i, k);
		}
		double meanReturn = sum / (M - 1);
		sum = 0;
		for (int i = 1; i < M; i++) {
			sum += Math.pow(returnI(i, k) - meanReturn, 2);
		}
		return Math.pow(sum / (deltaT * (M - 1)), 0.5);
	}
	
	public double deltaWJ() {
		return Math.pow(deltaT, 0.5) * r.nextGaussian();
	}
	
	public double sjp1(int jp1, int k) {
		double sj;
		int j = jp1 - 1;
		if (jp1 < pastPrices.length) {
			throw new smException("invalid index");
		} else if (jp1 == pastPrices.length) {
			sj = pastPrices[j];
		} else {
			sj = predictedPrices.get(k).get(j - pastPrices.length);
		}
		return sj + this.returnRateJ(j, k)*sj*deltaT + this.volatilityJ(j, k)*sj*this.deltaWJ();
	}
	
	public static boolean isNum(String str) { 
		try {  
			Double.parseDouble(str);  
			return true;
		} catch(NumberFormatException e){  
			return false;  
		}  
	}
	
	public static void main(String[] args) {
		String[] files = {"VOO"};
		GBMAP[] S = new GBMAP[files.length];
		
		for (int k = 0; k < files.length; k++) {
			String currentFile = files[k] + ".csv";
			String currentStatsName = files[k] + "Stats.data";
			String currentPredictedPricesName = files[k] + "PredictedPrices.data";
			ArrayList<Double> alpastPrices = new ArrayList<>();
			ArrayList<LocalDate> currentDates = new ArrayList<>();

			try {
				BufferedReader csvReader = new BufferedReader(new FileReader(currentFile));
				String row;
				while ((row = csvReader.readLine()) != null) {
					String[] data = row.split(",");
					if (isNum(data[4]) == true) {
						final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
						if (currentDates.size() == 0) {
							currentDates.add(LocalDate.parse(data[0], formatter));
						} else {
							currentDates.add(LocalDate.parse(data[0], formatter));
							while (ChronoUnit.DAYS.between(currentDates.get(currentDates.size() - 2), currentDates.get(currentDates.size() - 1)) != 1) {
								currentDates.remove(currentDates.size() - 1);
								currentDates.add(currentDates.get(currentDates.size() - 1).plus(1, ChronoUnit.DAYS));
								double[] app = new double[alpastPrices.size()];
								for (int i = 0; i < alpastPrices.size(); i++) {
									app[i] = alpastPrices.get(i);
								}
								if (currentDates.size() < 3) {
									alpastPrices.add(Double.parseDouble(data[4]));
								} else {
									GBMAP temp = new GBMAP(app);
									temp.initPredictedPrices(1);
									double sum = 0;
									for (int i = 0; i < 3; i++) {
										sum += temp.sjp1(alpastPrices.size(), 0);
									}
									alpastPrices.add(sum/3);
									temp = null;
								}
								app = null;
								currentDates.add(LocalDate.parse(data[0], formatter));
							}
						}
						alpastPrices.add(Double.parseDouble(data[4]));
					}
				}
				csvReader.close();
			
			} catch (IOException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			}

			double[] currentpastPrices = new double[alpastPrices.size()];
			for (int i = 0; i < alpastPrices.size(); i++) {
				currentpastPrices[i] = alpastPrices.get(i);
			}

			int startDateY  = currentDates.get(0).getYear();
			int startDateM = currentDates.get(0).getMonthValue();
			int startDateD   = currentDates.get(0).getDayOfMonth();
			long numOfDates = ChronoUnit.DAYS.between(currentDates.get(0), currentDates.get(currentDates.size() - 1));

			S[k] = new GBMAP(currentpastPrices);
			S[k].G(730*S[0].deltaT, 10, currentStatsName, currentPredictedPricesName, startDateY, startDateM, startDateD, numOfDates);
		}
	}
}