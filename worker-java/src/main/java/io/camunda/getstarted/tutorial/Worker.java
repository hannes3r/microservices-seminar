package io.camunda.getstarted.tutorial;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;

import java.sql.*;

class Concert {
  private String band;
  private double price;
  private int numberOfTickets;

  public Concert(String band, double price, int numberOfTickets) {
    this.band = band;
    this.price = price;
    this.numberOfTickets = numberOfTickets;
  }

  public String getBand() {
    return this.band;
  }

  public double getPrice() {
    return this.price;
  }

  public int getNumberOfTickets() {
    return this.numberOfTickets;
  }
}

@SpringBootApplication
@EnableZeebeClient
public class Worker {

  private static Connection c;

  public static void main(String[] args) {
    SpringApplication.run(Worker.class, args);

    c = null;

    try {
      Class.forName("org.sqlite.JDBC");
      c = DriverManager.getConnection("jdbc:sqlite:Main.db");
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
    System.out.println("Opened database successfully");
  }

  @JobWorker(type = "get-list-of-concerts")
  public Map<String, Object> getListOfConcerts(final ActivatedJob job) {

    List<String> bands = new ArrayList<>();

    Statement stmt = null;
    ResultSet rs = null;

    try {
      stmt = c.createStatement();
      rs = stmt.executeQuery("SELECT * FROM concerts");
      while (rs.next()) {
        bands.add(rs.getString("band"));
      }

      rs.close();
      stmt.close();

    } catch (SQLException e) {
      e.printStackTrace();
    }

    System.out.println(bands);

    // Probably add some process variables
    HashMap<String, Object> variables = new HashMap<>();
    variables.put("concert_list", bands);
    return variables;
  }

  @JobWorker(type = "check-ticket-availability")
  public Map<String, Object> checkTicketAvailability(final ActivatedJob job) {

    String band = (String) job.getVariablesAsMap().get("concert");

    Statement stmt = null;
    ResultSet rs = null;

    HashMap<String, Object> variables = new HashMap<>();

    try {
      stmt = c.createStatement();
      rs = stmt.executeQuery("SELECT c.numberOfTickets  FROM concerts c WHERE c.band = '" + band + "'");
      int numberOfTickets = 0;
      System.out.println("SELECT c.numberOfTickets  FROM concerts c WHERE c.band = '" + band + "'");
      System.out.println(band);

      while (rs.next()) {
        numberOfTickets = rs.getInt("numberOfTickets");
      }
      System.out.println(numberOfTickets);

      rs = stmt.executeQuery("SELECT COUNT() as count FROM bookings b WHERE band = '" + band + "'");
      while (rs.next()) {
        variables.put("is_available", rs.getInt("count") < numberOfTickets);
      }
      rs.close();
      stmt.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return variables;
  }

}
