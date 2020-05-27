package com.uptech.accounted.controller;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.chrono.HijrahChronology;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Controller;

import com.uptech.accounted.Main;
import com.uptech.accounted.bean.Ledger;
import com.uptech.accounted.bean.Master;
import com.uptech.accounted.bean.Subledger;
import com.uptech.accounted.bean.Transaction;
import com.uptech.accounted.bean.TransactionType;
import com.uptech.accounted.config.StageManager;
import com.uptech.accounted.repository.DepartmentRepository;
import com.uptech.accounted.repository.InitiatorRepository;
import com.uptech.accounted.repository.RecipientRepository;
import com.uptech.accounted.service.LedgerServiceImpl;
import com.uptech.accounted.service.SubledgerServiceImpl;
import com.uptech.accounted.service.TransactionServiceImpl;
import com.uptech.accounted.utils.ColumnDateFormatter;
import com.uptech.accounted.utils.ColumnHijriDateFormatter;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Pagination;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import lombok.extern.log4j.Log4j;

@Controller
@Log4j
public class TransactionController implements Initializable {

  @FXML
  private ComboBox<String> cbInitiator;

  @FXML
  private ComboBox<String> cbDepartment;

  @FXML
  private ComboBox<String> cbRecipient;

  @FXML
  private ComboBox<String> cbLedgerType;

  @FXML
  private ComboBox<String> cbSubledgerType;

  @FXML
  private ComboBox<TransactionType> cbTransactionType;

  @FXML
  private DatePicker dateOfTransaction;

  @FXML
  private TextField amount;

  @FXML
  private TextArea narration;

  @FXML
  private Button reset;

  @FXML
  private Button saveTransaction;

  @FXML
  private TableView<Transaction> transactionTable;

  @FXML
  private Pagination transactionPagination;

  @FXML
  private TableColumn<Transaction, Long> colTransactionId;

  @FXML
  private TableColumn<Transaction, String> colInitiator;

  @FXML
  private TableColumn<Transaction, String> colDepartment;

  @FXML
  private TableColumn<Transaction, LocalDate> colDateOfTransaction;

  @FXML
  private TableColumn<Transaction, LocalDate> colHijriDateOfTransaction;

  @FXML
  private TableColumn<Transaction, String> colRecipient;

  @FXML
  private TableColumn<Transaction, String> colLedgerType;

  @FXML
  private TableColumn<Transaction, String> colSubledgerType;

  @FXML
  private TableColumn<Transaction, BigDecimal> colAmount;

  @FXML
  private TableColumn<Transaction, String> colNarration;

  @FXML
  private TableColumn<Transaction, String> colTransactionType;

  @FXML
  private TableColumn<Transaction, Boolean> colEdit;

  @FXML
  private Button deleteTransactions;

  @FXML
  private Button saveResults;

  @FXML
  private TextField searchTextField;

  @FXML
  private Button searchTransactions;

  @Lazy
  @Autowired
  private StageManager stageManager;

  @Autowired
  private InitiatorRepository initiatorRepository;

  @Autowired
  private DepartmentRepository departmentRepository;

  @Autowired
  private LedgerServiceImpl ledgerServiceImpl;

  @Autowired
  private SubledgerServiceImpl subledgerServiceImpl;

  @Autowired
  private TransactionServiceImpl transactionServiceImpl;

  @Autowired
  private RecipientRepository recipientRepository;

  @Autowired
  private TransactionServiceImpl transactionService;

  private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
  private ObservableList<String> subledgerComboList = FXCollections.observableArrayList();
  private ObservableList<String> ledgerComboList = FXCollections.observableArrayList();

  @FXML
  TextField id;

  private int itemsPerPage = 25;

  @FXML
  private void exit(ActionEvent event) {
    Platform.exit();
  }

  @FXML
  void reset(ActionEvent event) {
    clearFields();
  }

  private void clearFields() {
    id.clear();
    cbInitiator.getSelectionModel().clearSelection();
    cbDepartment.getSelectionModel().clearSelection();
    // dateOfTransaction.getEditor().clear();
    cbRecipient.getSelectionModel().clearSelection();
    cbLedgerType.getSelectionModel().clearSelection();
    cbSubledgerType.getSelectionModel().clearSelection();
    cbTransactionType.getSelectionModel().clearSelection();
    amount.clear();
    narration.clear();
    saveTransaction.setText("Save");
  }

  @FXML
  private void saveTransaction(ActionEvent event) {
    Transaction transaction = new Transaction();
    if (getId() > 0)
      transaction = transactionServiceImpl.findById(getId());
    transaction.setInitiator(initiatorRepository.findOne(getInitiator()));
    transaction.setDepartment(departmentRepository.findOne(getDepartment()));
    transaction.setDateOfTransaction(getDateOfTransaction());
    transaction.setRecipient(recipientRepository.findOne(getRecipient()));
    transaction
        .setSubledgerType(subledgerServiceImpl.findByLedgerAndSubledgerCode((getLedgerCode()), getSubledgerCode()));
    transaction.setLedgerType(ledgerServiceImpl.findByCode(getLedgerCode()));
    BigDecimal transactionAmount = new BigDecimal(getAmount().replace(",", ""));
    transaction.setAmount(transactionAmount);
    transaction.setNarration(getNarration());
    transaction.setTransactionType(getTransactionType());

    Transaction newTransaction = transactionService.save(transaction);
    loadTransactionDetails();
    saveTransaction.setText("Save");
    saveAlert(newTransaction);
    clearFields();
  }

  @FXML
  private void deleteTransactions(ActionEvent event) {
    Transaction transaction = transactionTable.getSelectionModel().getSelectedItem();
    transactionService.delete(transaction);
    loadTransactionDetails();
  }

  @FXML
  private void saveSearchResult(ActionEvent event) {
    ReportsController.save(event, getSearchTransaction());
  }

  private void saveAlert(Transaction transaction) {

    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle("Transaction saved successfully.");
    alert.setHeaderText(null);
    alert.setContentText("New entry created : \n" + transaction.msgWhenSaved());
    alert.showAndWait();
  }

  public String getInitiator() {
    return cbInitiator.getSelectionModel().getSelectedItem().split("-")[0];
  }

  public String getDepartment() {
    return cbDepartment.getSelectionModel().getSelectedItem().split("-")[0];
  }

  public TransactionType getTransactionType() {
    return cbTransactionType.getSelectionModel().getSelectedItem();
  }

  public String getRecipient() {
    return cbRecipient.getSelectionModel().getSelectedItem().split("-")[0];
  }

  public String getLedgerCode() {
    String ledgerCode = "";
    try {
      ledgerCode = cbLedgerType.getSelectionModel().getSelectedItem().split("-")[0];
    } catch (NullPointerException npe) {
      log.error("Ledger Type Selection is empty");
    }
    return ledgerCode;
  }

  public String getSubledgerCode() {
    String subledgerCode = "";
    try {
      subledgerCode = cbSubledgerType.getSelectionModel().getSelectedItem().split("-")[0];
    } catch (NullPointerException npe) {
      log.error("Subledger Type Selection is empty");
    }
    return subledgerCode;
  }

  public LocalDate getDateOfTransaction() {
    return dateOfTransaction.getValue();
  }

  public String getAmount() {
    return amount.getText();
  }

  public String getNarration() {
    return narration.getText();
  }

  public long getId() {
    try {
      return Long.parseLong(id.getText());
    } catch (Exception exception) {
      // Do nothing. This will be a new entry case
    }
    return 0;
  }

  private void loadTransactionDetails() {
    transactionList.clear();

    if (searchTextField.getText().isEmpty()) {
      int count = (int) transactionService.count();
      transactionPagination.setPageCount(getPageCount(count));
      transactionPagination.setPageFactory(this::createPage);
      Iterable<Transaction> all = getTransactionsForPage(0);
      all.forEach(transaction -> transactionList.add(transaction));

      transactionTable.setItems(transactionList);
    } else {
      int count = 1;
      transactionPagination.setPageCount(getPageCount(count));
      transactionPagination.setPageFactory(this::createPage);
      List<Transaction> searchedTransaction = getSearchTransaction();
      transactionList.addAll(searchedTransaction);

      transactionTable.setItems(transactionList);
    }
  }

  private int getPageCount(int count) {
    if (count == 0)
      return 1;
    return count % itemsPerPage == 0 ? count / itemsPerPage : count / itemsPerPage + 1;
  }

  private Iterable<Transaction> getTransactionsForPage(int pageNumer) {
    PageRequest request = new PageRequest(pageNumer, itemsPerPage,
        new Sort(new Sort.Order(Sort.Direction.DESC, "dateOfTransaction")));
    return transactionService.findAll(request);
  }

  private List<Transaction> getSearchTransaction() {
    return transactionService.fullTextSearch(searchTextField.getText());
  }

  @SuppressWarnings("unchecked")
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    id.setDisable(true);
    loadTransactionTypes();
    loadInitiators();
    loadDepartments();
    loadLedgers();
    loadRecipients();
    makeAmountFieldNumericOnly();
    HijrahChronology hijriChronology = HijrahChronology.INSTANCE;
    dateOfTransaction.setChronology(hijriChronology);

    dateOfTransaction.setConverter(new StringConverter<LocalDate>() {
      String pattern = "dd-MM-yyyy";
      DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);

      {
        dateOfTransaction.setPromptText(pattern.toLowerCase());
      }

      @Override
      public String toString(LocalDate date) {
        if (date != null) {
          return dateFormatter.format(date);
        } else {
          return "";
        }
      }

      @Override
      public LocalDate fromString(String string) {
        if (string != null && !string.isEmpty()) {
          return LocalDate.parse(string, dateFormatter);
        } else {
          return null;
        }
      }
    });

//    cbSubledgerType.addEventHandler(KeyEvent.KEY_PRESSED, new AutoCompleteComboBoxListener(cbSubledgerType));

    transactionTable.setOnMouseClicked(event -> {
      try {
        Transaction selectedItem = transactionTable.getSelectionModel().getSelectedItem();
        id.setText(String.valueOf(selectedItem.getTransactionId()));
        cbInitiator.getSelectionModel()
            .select(selectedItem.getInitiator().getCode() + "-" + selectedItem.getInitiator().getName());
        cbDepartment.getSelectionModel()
            .select(selectedItem.getDepartment().getCode() + "-" + selectedItem.getDepartment().getName());
        cbRecipient.getSelectionModel()
            .select(selectedItem.getRecipient().getCode() + "-" + selectedItem.getRecipient().getName());
        cbLedgerType.getSelectionModel()
            .select(selectedItem.getLedgerType().getLedgerCode() + "-" + selectedItem.getLedgerType().getLedgerName());
        cbSubledgerType.getSelectionModel().select(selectedItem.getSubledgerType().getSubledgerId().getSubledgerCode()
            + "-" + selectedItem.getSubledgerType().getSubledgerName());
        cbTransactionType.getSelectionModel().select(selectedItem.getTransactionType());
        dateOfTransaction.setValue(selectedItem.getDateOfTransaction());
        amount.setText(getIndianCurrencyFormat(selectedItem.getAmount()));
        narration.setText(selectedItem.getNarration().toString());
        saveTransaction.setText("Update");
      } catch (Exception exception) {
        // No rows selected in the table
      }
    });
    transactionTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    setColumnProperties();
    // Add all transactions into table
    loadTransactionDetails();
  }

  public static String formatLakh(double d) {
    String s = String.format(Locale.UK, "%1.2f", Math.abs(d));
    s = s.replaceAll("(.+)(...\\...)", "$1,$2");
    while (s.matches("\\d{3,},.+")) {
      s = s.replaceAll("(\\d+)(\\d{2},.+)", "$1,$2");
    }
    return d < 0 ? ("-" + s) : s;
  }

  private String getIndianCurrencyFormat(BigDecimal newValue) {
    // DecimalFormat formatter = new DecimalFormat("#,##,##,##,##,###.00");
    // String newValueStr = formatter.format(newValue);
    // return newValueStr;
    return formatLakh(newValue.doubleValue());
  }

  public static String formatIndianCommaSeparated(String rupee) {
    Locale indiaLocale = new Locale("en", "IN");
    NumberFormat india = NumberFormat.getCurrencyInstance(indiaLocale);
    return india.format(new BigDecimal(rupee));
  }

  private void makeAmountFieldNumericOnly() {
    amount.focusedProperty().addListener((obs, inFocus, outFocus) -> {
      if (outFocus) {
        String number = amount.getText();
        if (number != "" && number != "[]") {
          number = number.replaceAll(",", "");
          try {
            if (Main.isNumeric(number))
              amount.setText(getIndianCurrencyFormat(new BigDecimal(number)));
          } catch (Exception e) {
            return;
          }
        }
      }
    });
  }

  private void loadInitiators() {
    loadMaster(initiatorRepository, cbInitiator);
  }

  private void loadDepartments() {
    loadMaster(departmentRepository, cbDepartment);
  }

  private void loadTransactionTypes() {
    List<TransactionType> transactionTypes = new ArrayList<>();
    transactionTypes = Arrays.asList(TransactionType.values());

    ObservableList<TransactionType> comboList = FXCollections.observableArrayList(transactionTypes);
    cbTransactionType.setItems(comboList);
  }

  private void loadRecipients() {
    loadMaster(recipientRepository, cbRecipient);
  }

  private <T extends Master> void loadMaster(JpaRepository<T, String> repository, ComboBox<String> comboBox) {
    List<T> list = repository.findAll();
    ObservableList<String> comboList = FXCollections.observableArrayList();
    for (T item : list) {
      comboList.add(item.getCode() + "-" + item.getName());
    }
    comboBox.setItems(comboList);
    comboBox.addEventHandler(KeyEvent.KEY_PRESSED, new AutoCompleteComboBoxListener<T>(comboBox));
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void loadLedgers() {
    ledgerComboList.clear();
    List<Ledger> ledgerList = ledgerServiceImpl.findAll();
    for (Ledger ledger : ledgerList) {
      ledgerComboList.add(ledger.getLedgerCode() + "-" + ledger.getLedgerName());
    }
    cbLedgerType.setItems(ledgerComboList);
    cbLedgerType.addEventHandler(KeyEvent.KEY_PRESSED, new AutoCompleteComboBoxListener(cbLedgerType));
  }

  @FXML
  public void loadSubledgers(ActionEvent event) {
    subledgerComboList.clear();
    List<Subledger> subledgerList = subledgerServiceImpl.findByLedgerCode(getLedgerCode());
    for (Subledger subledger : subledgerList) {
      subledgerComboList.add(subledger.getSubledgerId().getSubledgerCode() + "-" + subledger.getSubledgerName());
    }
    cbSubledgerType.setItems(subledgerComboList);
    cbSubledgerType.addEventHandler(KeyEvent.KEY_PRESSED, new AutoCompleteComboBoxListener(cbSubledgerType));
  }

  private void setColumnProperties() {
    colTransactionId.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
    colInitiator.setCellValueFactory(new PropertyValueFactory<>("initiatorName"));
    colDepartment.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
    colLedgerType.setCellValueFactory(new PropertyValueFactory<>("ledgerName"));
    colRecipient.setCellValueFactory(new PropertyValueFactory<>("recipientName"));
    colTransactionType.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
    colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
    colNarration.setCellValueFactory(new PropertyValueFactory<>("narration"));
    colSubledgerType.setCellValueFactory(new PropertyValueFactory<>("subledgerName"));

    colDateOfTransaction.setCellValueFactory(new PropertyValueFactory<>("dateOfTransaction"));
    colDateOfTransaction.setCellFactory(new ColumnDateFormatter<>(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

    colHijriDateOfTransaction.setCellValueFactory(new PropertyValueFactory<>("dateOfTransaction"));
    colHijriDateOfTransaction
        .setCellFactory(new ColumnHijriDateFormatter<>(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
    colAmount.setCellFactory(getCustomCellFactory());
  }

  private Node createPage(int pageIndex) {
    transactionTable.setItems(FXCollections.observableArrayList(
        StreamSupport.stream(getTransactionsForPage(pageIndex).spliterator(), false).collect(Collectors.toList())));
    return transactionTable;
  }

  public void searchTransactions(ActionEvent actionEvent) {
    loadTransactionDetails();
  }

  public void searchTransactionsOnEnter(KeyEvent keyEvent) {
    if (keyEvent.getCode() == KeyCode.ENTER)
      loadTransactionDetails();
  }

  private Callback<TableColumn<Transaction, BigDecimal>, TableCell<Transaction, BigDecimal>> getCustomCellFactory() {
    return new Callback<TableColumn<Transaction, BigDecimal>, TableCell<Transaction, BigDecimal>>() {
      @Override
      public TableCell<Transaction, BigDecimal> call(TableColumn<Transaction, BigDecimal> param) {
        TableCell<Transaction, BigDecimal> cell = new TableCell<Transaction, BigDecimal>() {

          @Override
          public void updateItem(final BigDecimal item, boolean empty) {
            if (!empty) {
              if (item.doubleValue() > 0)
                setText(getIndianCurrencyFormat(item));
              // setStyle("-fx-text-fill: " + color + ";");
            } else {
              setText("");
            }
          }
        };
        return cell;
      }
    };
  }

}
