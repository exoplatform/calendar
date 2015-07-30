/**
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/

package org.exoplatform.calendar.service;

import java.util.LinkedList;
import java.util.List;

public class QueryCondition {
  private List<QueryCondition> andConditions = new LinkedList<QueryCondition>();
  private List<QueryCondition> orConditions = new LinkedList<QueryCondition>();
  
  private List<Expression<?>> expressions = new LinkedList<Expression<?>>();
  
  protected QueryCondition parent;
  
  public QueryCondition and(QueryCondition other) {
    other.parent = this;
    andConditions.add(other);
    return this;
  }
  
  public QueryCondition or(QueryCondition other) {
    other.parent = this;
    orConditions.add(other);
    return this;
  }
  
  public QueryCondition with(Expression<?> expression) {
    expressions.add(expression);
    return this;
  }
  
  public List<Expression<?>> getExpressions() {
    return new LinkedList<Expression<?>>(expressions);
  }
  
  public List<QueryCondition> getAndConditions() {
    return andConditions;
  }

  public List<QueryCondition> getOrConditions() {
    return orConditions;
  }

  public static class Expression<T> {
    private String name;
    private T value;
    private Operator operator;
    public static enum Operator {
      EQUAL, CONTAIN, NOT_EQUAL, GREATER, SMALLER, GREATER_OR_EQUAL, SMALLER_OR_EQUAL
    }
    
    private Expression(String name, T value, Operator operator) {
      if (name == null) {
        throw new IllegalArgumentException("name must not be null");
      }
      this.name = name;
      this.value = value;
      this.operator = operator;
    }
    
    public static <T> Expression<T> equal(String name, T value) {
      return new Expression<T>(name, value, Operator.EQUAL);
    }
    
    public static <T> Expression<T> not_equal(String name, T value) {
      return new Expression<T>(name, value, Operator.NOT_EQUAL);
    }
    
    public static <T> Expression<T> contain(String name, T value) {
      return new Expression<T>(name, value, Operator.CONTAIN);
    }
    
    public static <T> Expression<T> greater(String name, T value) {
      return new Expression<T>(name, value, Operator.GREATER);
    }
    
    public static <T> Expression<T> smaller(String name, T value) {
      return new Expression<T>(name, value, Operator.SMALLER);
    }
    
    public static <T> Expression<T> greater_or_equal(String name, T value) {
      return new Expression<T>(name, value, Operator.GREATER_OR_EQUAL);
    }
    
    public static <T> Expression<T> smaller_or_equal(String name, T value) {
      return new Expression<T>(name, value, Operator.SMALLER_OR_EQUAL);
    }

    public String getName() {
      return name;
    }

    public T getValue() {
      return value;
    }

    public Operator getOperator() {
      return operator;
    }
    
  }
}
