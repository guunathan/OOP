import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

interface RateProvider {
    double getRate(String from, String to) throws IOException;
}

class ApiRateProvider implements RateProvider {

    @Override
    public double getRate(String from, String to) throws IOException {
        String api = "https://api.frankfurter.dev/v1/latest?base=EUR&symbols=USD" + from + "&symbols=" + to;

        HttpURLConnection conn = (HttpURLConnection) new URL(api).openConnection();
        conn.setRequestMethod("GET");

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder json = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            json.append(line);
        }
        br.close();

        JSONObject obj = new JSONObject(json.toString());
        return obj.getJSONObject("rates").getDouble(to);
    }
}

class Account {
    private double usdBalance = 0.0;
    private Map<String, Double> foreignHoldings = new HashMap<>();

    private RateProvider rateProvider;

    public Account(RateProvider rateProvider) {
        this.rateProvider = rateProvider;
    }

    public void deposit(double amount, String currency) throws IOException {
        double rate = rateProvider.getRate(currency, "USD");
        double usd = amount * rate;
        usdBalance += usd;
        System.out.println("DEPOSIT: " + amount + " " + currency + " => " + usd + " USD stored.");
    }

    public void withdraw(double amountUSD) {
        if (amountUSD > usdBalance) {
            System.out.println("❌ Withdraw failed: Not enough USD balance.");
            return;
        }
        usdBalance -= amountUSD;
        System.out.println("WITHDRAW: " + amountUSD + " USD successful.");
    }

    public void exchangeTo(String targetCurrency, double amountUSD) throws IOException {
        if (targetCurrency.equalsIgnoreCase("USD")) {
            System.out.println("❌ Cannot convert back to USD!");
            return;
        }

        if (amountUSD > usdBalance) {
            System.out.println("❌ Not enough USD to exchange.");
            return;
        }

        double rate = rateProvider.getRate("USD", targetCurrency);
        double foreignAmount = amountUSD * rate;

        usdBalance -= amountUSD;
        foreignHoldings.put(targetCurrency, foreignHoldings.getOrDefault(targetCurrency, 0.0) + foreignAmount);

        System.out.println("EXCHANGE: " + amountUSD + " USD => " + foreignAmount + " " + targetCurrency);
    }

    public void showPortfolio() {
        System.out.println("\n===== ACCOUNT PORTFOLIO =====");
        System.out.println("USD Balance: " + usdBalance);
        System.out.println("Foreign Holdings: " + foreignHoldings + "\n");
    }
}
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        RateProvider provider = new ApiRateProvider();
        Account account = new Account(provider);

        try {
            while (true) {
                System.out.println("\n====== MENU ======");
                System.out.println("1) Deposit");
                System.out.println("2) Withdraw");
                System.out.println("3) Exchange USD -> Foreign");
                System.out.println("4) View Portfolio");
                System.out.println("5) Exit");
                System.out.print("Choose: ");

                int choice = sc.nextInt();

                if (choice == 1) {
                    System.out.print("Enter amount: ");
                    double amt = sc.nextDouble();
                    System.out.print("Enter currency (USD/EUR/JPY/GBP/THB): ");
                    String c = sc.next().toUpperCase();
                    account.deposit(amt, c);
                }
                else if (choice == 2) {
                    System.out.print("Enter USD to withdraw: ");
                    account.withdraw(sc.nextDouble());
                }
                else if (choice == 3) {
                    System.out.print("Enter USD amount to exchange: ");
                    double amt = sc.nextDouble();
                    System.out.print("Enter target currency (USD/EUR/JPY/GBP/THB): ");
                    String c = sc.next().toUpperCase();
                    account.exchangeTo(c, amt);
                }
                else if (choice == 4) {
                    account.showPortfolio();
                }
                else if (choice == 5) {
                    System.out.println("Goodbye!");
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error → " + e.getMessage());
        }

        sc.close();
    }
}

