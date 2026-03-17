package com.demo.client.ui;

import java.util.function.Function;
import javafx.scene.control.TableCell;

public class PrettyCell<S, T> extends TableCell<S, T> {
  private final Function<T, String> formatter;

  public PrettyCell(Function<T, String> formatter) {
    this.formatter = formatter;
    getStyleClass().add("pretty-cell");
  }

  @Override
  protected void updateItem(T item, boolean empty) {
    super.updateItem(item, empty);
    if (empty) {
      setText(null);
      return;
    }
    setText(formatter.apply(item));
  }
}

