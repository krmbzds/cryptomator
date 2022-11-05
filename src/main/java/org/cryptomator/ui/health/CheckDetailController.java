package org.cryptomator.ui.health;

import com.tobiasdiez.easybind.EasyBind;
import com.tobiasdiez.easybind.EasyObservableList;
import com.tobiasdiez.easybind.Subscription;
import org.cryptomator.cryptofs.health.api.DiagnosticResult;
import org.cryptomator.ui.common.FxController;

import javax.inject.Inject;
import javafx.beans.binding.Binding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import java.util.function.Function;
import java.util.stream.Stream;

@HealthCheckScoped
public class CheckDetailController implements FxController {

	private final EasyObservableList<Result> results;
	private final ObjectProperty<Check> check;
	private final ObservableValue<Check.CheckState> checkState;
	private final ObservableValue<String> checkName;
	private final BooleanExpression checkRunning;
	private final BooleanExpression checkScheduled;
	private final BooleanExpression checkFinished;
	private final BooleanExpression checkSkipped;
	private final BooleanExpression checkSucceeded;
	private final BooleanExpression checkFailed;
	private final BooleanExpression checkCancelled;
	private final Binding<Number> countOfWarnSeverity;
	private final Binding<Number> countOfCritSeverity;
	private final Binding<Boolean> warnOrCritsExist;
	private final ResultListCellFactory resultListCellFactory;

	public ListView<Result> resultsListView;
	private Subscription resultSubscription;

	@Inject
	public CheckDetailController(ObjectProperty<Check> selectedTask, ResultListCellFactory resultListCellFactory) {
		this.resultListCellFactory = resultListCellFactory;
		this.results = EasyBind.wrapList(FXCollections.observableArrayList());
		this.check = selectedTask;
		this.checkState = selectedTask.flatMap(Check::stateProperty);
		this.checkName = selectedTask.map(Check::getName).orElse("");
		this.checkRunning = BooleanExpression.booleanExpression(checkState.map(Check.CheckState.RUNNING::equals).orElse(false));
		this.checkScheduled = BooleanExpression.booleanExpression(checkState.map(Check.CheckState.SCHEDULED::equals).orElse(false));
		this.checkSkipped =BooleanExpression.booleanExpression(checkState.map(Check.CheckState.SKIPPED::equals).orElse(false));
		this.checkSucceeded = BooleanExpression.booleanExpression(checkState.map(Check.CheckState.SUCCEEDED::equals).orElse(false));
		this.checkFailed = BooleanExpression.booleanExpression(checkState.map(Check.CheckState.ERROR::equals).orElse(false));
		this.checkCancelled = BooleanExpression.booleanExpression(checkState.map(Check.CheckState.CANCELLED::equals).orElse(false));
		this.checkFinished = checkSucceeded.or(checkFailed).or(checkCancelled);
		this.countOfWarnSeverity = results.reduce(countSeverity(DiagnosticResult.Severity.WARN));
		this.countOfCritSeverity = results.reduce(countSeverity(DiagnosticResult.Severity.CRITICAL));
		this.warnOrCritsExist = EasyBind.combine(checkSucceeded, countOfWarnSeverity, countOfCritSeverity, (suceeded, warns, crits) -> suceeded && (warns.longValue() > 0 || crits.longValue() > 0) );
		selectedTask.addListener(this::selectedTaskChanged);
	}

	private void selectedTaskChanged(ObservableValue<? extends Check> observable, Check oldValue, Check newValue) {
		if (resultSubscription != null) {
			resultSubscription.unsubscribe();
		}
		if (newValue != null) {
			resultSubscription = EasyBind.bindContent(results, newValue.getResults());
		}
	}

	private Function<Stream<? extends Result>, Long> countSeverity(DiagnosticResult.Severity severity) {
		return stream -> stream.filter(item -> severity.equals(item.diagnosis().getSeverity())).count();
	}

	@FXML
	public void initialize() {
		resultsListView.setItems(results);
		resultsListView.setCellFactory(resultListCellFactory);
	}

	/* Getter/Setter */

	public String getCheckName() {
		return checkName.getValue();
	}

	public ObservableValue<String> checkNameProperty() {
		return checkName;
	}

	public long getCountOfWarnSeverity() {
		return countOfWarnSeverity.getValue().longValue();
	}

	public Binding<Number> countOfWarnSeverityProperty() {
		return countOfWarnSeverity;
	}

	public long getCountOfCritSeverity() {
		return countOfCritSeverity.getValue().longValue();
	}

	public Binding<Number> countOfCritSeverityProperty() {
		return countOfCritSeverity;
	}

	public boolean isCheckRunning() {
		return checkRunning.getValue();
	}

	public BooleanExpression checkRunningProperty() {
		return checkRunning;
	}

	public boolean isCheckFinished() {
		return checkFinished.getValue();
	}

	public BooleanExpression checkFinishedProperty() {
		return checkFinished;
	}

	public boolean isCheckScheduled() {
		return checkScheduled.getValue();
	}

	public BooleanExpression checkScheduledProperty() {
		return checkScheduled;
	}

	public boolean isCheckSkipped() {
		return checkSkipped.getValue();
	}

	public BooleanExpression checkSkippedProperty() {
		return checkSkipped;
	}

	public boolean isCheckSucceeded() {
		return checkSucceeded.getValue();
	}

	public BooleanExpression checkSucceededProperty() {
		return checkSucceeded;
	}

	public boolean isCheckFailed() {
		return checkFailed.getValue();
	}

	public BooleanExpression checkFailedProperty() {
		return checkFailed;
	}

	public boolean isCheckCancelled() {
		return checkCancelled.getValue();
	}

	public Binding<Boolean> warnOrCritsExistProperty() {
		return warnOrCritsExist;
	}

	public boolean isWarnOrCritsExist() {
		return warnOrCritsExist.getValue();
	}

	public BooleanExpression checkCancelledProperty() {
		return checkCancelled;
	}

	public ObjectProperty<Check> checkProperty() {
		return check;
	}

	public Check getCheck() {
		return check.get();
	}
}
