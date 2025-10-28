import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Scanner;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class convert {

    public static void main(String[] args) {

        HashMap<Integer, String> currency = new HashMap<>();
        currency.put(1, "USD");
        currency.put(2, "INR");
        currency.put(3, "EUR");
        currency.put(4, "GBP");
        currency.put(5, "JPY");

        String current;
        String fromcurrent;
        double amount;

        Scanner sc = new Scanner(System.in);

        System.out.println("Welcome to the currency converter");
        System.out.println("Currency converting From?");
        System.out.println("1.USD (US dollar)\n2.INR (Indian Rupee)\n3.EUR (Euro)\n4.GBP (British Pound)\n5.JPY (Japanese Yen)");

        fromcurrent = currency.get(sc.nextInt());

        System.out.println("Currency converting To?");
        System.out.println("1.USD (US dollar)\n2.INR (Indian Rupee)\n3.EUR (Euro)\n4.GBP (British Pound)\n5.JPY (Japanese Yen)");

        current = currency.get(sc.nextInt());

        System.out.println("Amount you wish to convert?");
        amount = sc.nextDouble();

        try {
            sendHTTPRequest(current, fromcurrent, amount);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println("Thank you for using the currency converter");
        sc.close();
    }

    private static void sendHTTPRequest(String current, String fromcurrent, double amount) throws IOException {
        //แก้ API ตรวนี้นะเฮีย
        String GOTURL = "https://api.exchangerate.host/latest?base=" + fromcurrent + "&symbols=" + current;

        URL url = new URL(GOTURL);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("GET");

        int responseCode = httpURLConnection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {

            BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // แปลง JSON
            JSONObject obj = new JSONObject(response.toString());
            double exchangeRate = obj.getJSONObject("rates").getDouble(current);

            System.out.println("Exchange rate: 1 " + fromcurrent + " = " + exchangeRate + " " + current);
            System.out.println(amount + " " + fromcurrent + " = " + (amount * exchangeRate) + " " + current);

        } else {
            System.out.println("GET request not worked. Response code: " + responseCode);
        }
    }
}
