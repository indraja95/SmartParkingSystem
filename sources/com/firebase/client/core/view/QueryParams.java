package com.firebase.client.core.view;

import com.firebase.client.core.view.filter.IndexedFilter;
import com.firebase.client.core.view.filter.LimitedFilter;
import com.firebase.client.core.view.filter.NodeFilter;
import com.firebase.client.core.view.filter.RangedFilter;
import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.Index;
import com.firebase.client.snapshot.Node;
import com.firebase.client.snapshot.NodeUtilities;
import com.firebase.client.snapshot.PriorityIndex;
import com.shaded.fasterxml.jackson.databind.ObjectMapper;
import com.shaded.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QueryParams {
    static final /* synthetic */ boolean $assertionsDisabled = (!QueryParams.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    public static final QueryParams DEFAULT_PARAMS = new QueryParams();
    private static final String INDEX = "i";
    private static final String INDEX_END_NAME = "en";
    private static final String INDEX_END_VALUE = "ep";
    private static final String INDEX_START_NAME = "sn";
    private static final String INDEX_START_VALUE = "sp";
    private static final String LIMIT = "l";
    private static final String VIEW_FROM = "vf";
    private static final ObjectMapper mapperInstance = new ObjectMapper();
    private Index index = PriorityIndex.getInstance();
    private ChildKey indexEndName = null;
    private Node indexEndValue = null;
    private ChildKey indexStartName = null;
    private Node indexStartValue = null;
    private String jsonSerialization = null;
    private Integer limit;
    private ViewFrom viewFrom;

    private enum ViewFrom {
        LEFT,
        RIGHT
    }

    static {
        mapperInstance.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public boolean hasStart() {
        if (this.indexStartValue != null) {
            return true;
        }
        return $assertionsDisabled;
    }

    public Node getIndexStartValue() {
        if (hasStart()) {
            return this.indexStartValue;
        }
        throw new IllegalArgumentException("Cannot get index start value if start has not been set");
    }

    public ChildKey getIndexStartName() {
        if (!hasStart()) {
            throw new IllegalArgumentException("Cannot get index start name if start has not been set");
        } else if (this.indexStartName != null) {
            return this.indexStartName;
        } else {
            return ChildKey.getMinName();
        }
    }

    public boolean hasEnd() {
        if (this.indexEndValue != null) {
            return true;
        }
        return $assertionsDisabled;
    }

    public Node getIndexEndValue() {
        if (hasEnd()) {
            return this.indexEndValue;
        }
        throw new IllegalArgumentException("Cannot get index end value if start has not been set");
    }

    public ChildKey getIndexEndName() {
        if (!hasEnd()) {
            throw new IllegalArgumentException("Cannot get index end name if start has not been set");
        } else if (this.indexEndName != null) {
            return this.indexEndName;
        } else {
            return ChildKey.getMaxName();
        }
    }

    public boolean hasLimit() {
        if (this.limit != null) {
            return true;
        }
        return $assertionsDisabled;
    }

    public boolean hasAnchoredLimit() {
        if (!hasLimit() || this.viewFrom == null) {
            return $assertionsDisabled;
        }
        return true;
    }

    public int getLimit() {
        if (hasLimit()) {
            return this.limit.intValue();
        }
        throw new IllegalArgumentException("Cannot get limit if limit has not been set");
    }

    public Index getIndex() {
        return this.index;
    }

    private QueryParams copy() {
        QueryParams params = new QueryParams();
        params.limit = this.limit;
        params.indexStartValue = this.indexStartValue;
        params.indexStartName = this.indexStartName;
        params.indexEndValue = this.indexEndValue;
        params.indexEndName = this.indexEndName;
        params.viewFrom = this.viewFrom;
        params.index = this.index;
        return params;
    }

    public QueryParams limit(int limit2) {
        QueryParams copy = copy();
        copy.limit = Integer.valueOf(limit2);
        copy.viewFrom = null;
        return copy;
    }

    public QueryParams limitToFirst(int limit2) {
        QueryParams copy = copy();
        copy.limit = Integer.valueOf(limit2);
        copy.viewFrom = ViewFrom.LEFT;
        return copy;
    }

    public QueryParams limitToLast(int limit2) {
        QueryParams copy = copy();
        copy.limit = Integer.valueOf(limit2);
        copy.viewFrom = ViewFrom.RIGHT;
        return copy;
    }

    public QueryParams startAt(Node indexStartValue2, ChildKey indexStartName2) {
        if ($assertionsDisabled || indexStartValue2.isLeafNode() || indexStartValue2.isEmpty()) {
            QueryParams copy = copy();
            copy.indexStartValue = indexStartValue2;
            copy.indexStartName = indexStartName2;
            return copy;
        }
        throw new AssertionError();
    }

    public QueryParams endAt(Node indexEndValue2, ChildKey indexEndName2) {
        if ($assertionsDisabled || indexEndValue2.isLeafNode() || indexEndValue2.isEmpty()) {
            QueryParams copy = copy();
            copy.indexEndValue = indexEndValue2;
            copy.indexEndName = indexEndName2;
            return copy;
        }
        throw new AssertionError();
    }

    public QueryParams orderBy(Index index2) {
        QueryParams copy = copy();
        copy.index = index2;
        return copy;
    }

    public boolean isViewFromLeft() {
        if (this.viewFrom == null) {
            return hasStart();
        }
        if (this.viewFrom == ViewFrom.LEFT) {
            return true;
        }
        return $assertionsDisabled;
    }

    public Map<String, Object> getWireProtocolParams() {
        Map<String, Object> queryObject = new HashMap<>();
        if (hasStart()) {
            queryObject.put(INDEX_START_VALUE, this.indexStartValue.getValue());
            if (this.indexStartName != null) {
                queryObject.put(INDEX_START_NAME, this.indexStartName.asString());
            }
        }
        if (hasEnd()) {
            queryObject.put(INDEX_END_VALUE, this.indexEndValue.getValue());
            if (this.indexEndName != null) {
                queryObject.put(INDEX_END_NAME, this.indexEndName.asString());
            }
        }
        if (this.limit != null) {
            queryObject.put(LIMIT, this.limit);
            ViewFrom viewFromToAdd = this.viewFrom;
            if (viewFromToAdd == null) {
                if (hasStart()) {
                    viewFromToAdd = ViewFrom.LEFT;
                } else {
                    viewFromToAdd = ViewFrom.RIGHT;
                }
            }
            switch (viewFromToAdd) {
                case LEFT:
                    queryObject.put(VIEW_FROM, LIMIT);
                    break;
                case RIGHT:
                    queryObject.put(VIEW_FROM, "r");
                    break;
            }
        }
        if (!this.index.equals(PriorityIndex.getInstance())) {
            queryObject.put(INDEX, this.index.getQueryDefinition());
        }
        return queryObject;
    }

    public boolean loadsAllData() {
        if (hasStart() || hasEnd() || hasLimit()) {
            return $assertionsDisabled;
        }
        return true;
    }

    public boolean isDefault() {
        if (!loadsAllData() || !this.index.equals(PriorityIndex.getInstance())) {
            return $assertionsDisabled;
        }
        return true;
    }

    public boolean isValid() {
        if (!hasStart() || !hasEnd() || !hasLimit() || hasAnchoredLimit()) {
            return true;
        }
        return $assertionsDisabled;
    }

    public String toJSON() {
        if (this.jsonSerialization == null) {
            try {
                this.jsonSerialization = mapperInstance.writeValueAsString(getWireProtocolParams());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this.jsonSerialization;
    }

    public static QueryParams fromQueryObject(Map<String, Object> map) {
        QueryParams params = new QueryParams();
        params.limit = (Integer) map.get(LIMIT);
        if (map.containsKey(INDEX_START_VALUE)) {
            params.indexStartValue = NodeUtilities.NodeFromJSON(map.get(INDEX_START_VALUE));
            String indexStartName2 = (String) map.get(INDEX_START_NAME);
            if (indexStartName2 != null) {
                params.indexStartName = ChildKey.fromString(indexStartName2);
            }
        }
        if (map.containsKey(INDEX_END_VALUE)) {
            params.indexEndValue = NodeUtilities.NodeFromJSON(map.get(INDEX_END_VALUE));
            String indexEndName2 = (String) map.get(INDEX_END_NAME);
            if (indexEndName2 != null) {
                params.indexEndName = ChildKey.fromString(indexEndName2);
            }
        }
        String viewFrom2 = (String) map.get(VIEW_FROM);
        if (viewFrom2 != null) {
            params.viewFrom = viewFrom2.equals(LIMIT) ? ViewFrom.LEFT : ViewFrom.RIGHT;
        }
        String indexStr = (String) map.get(INDEX);
        if (indexStr != null) {
            params.index = Index.fromQueryDefinition(indexStr);
        }
        return params;
    }

    public NodeFilter getNodeFilter() {
        if (loadsAllData()) {
            return new IndexedFilter(getIndex());
        }
        if (hasLimit()) {
            return new LimitedFilter(this);
        }
        return new RangedFilter(this);
    }

    public String toString() {
        return getWireProtocolParams().toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return $assertionsDisabled;
        }
        QueryParams that = (QueryParams) o;
        if (this.limit == null ? that.limit != null : !this.limit.equals(that.limit)) {
            return $assertionsDisabled;
        }
        if (this.index == null ? that.index != null : !this.index.equals(that.index)) {
            return $assertionsDisabled;
        }
        if (this.indexEndName == null ? that.indexEndName != null : !this.indexEndName.equals(that.indexEndName)) {
            return $assertionsDisabled;
        }
        if (this.indexEndValue == null ? that.indexEndValue != null : !this.indexEndValue.equals(that.indexEndValue)) {
            return $assertionsDisabled;
        }
        if (this.indexStartName == null ? that.indexStartName != null : !this.indexStartName.equals(that.indexStartName)) {
            return $assertionsDisabled;
        }
        if (this.indexStartValue == null ? that.indexStartValue != null : !this.indexStartValue.equals(that.indexStartValue)) {
            return $assertionsDisabled;
        }
        if (isViewFromLeft() != that.isViewFromLeft()) {
            return $assertionsDisabled;
        }
        return true;
    }

    public int hashCode() {
        int result;
        int i;
        int i2;
        int i3;
        int i4;
        int i5 = 0;
        if (this.limit != null) {
            result = this.limit.intValue();
        } else {
            result = 0;
        }
        int i6 = ((result * 31) + (isViewFromLeft() ? 1231 : 1237)) * 31;
        if (this.indexStartValue != null) {
            i = this.indexStartValue.hashCode();
        } else {
            i = 0;
        }
        int i7 = (i6 + i) * 31;
        if (this.indexStartName != null) {
            i2 = this.indexStartName.hashCode();
        } else {
            i2 = 0;
        }
        int i8 = (i7 + i2) * 31;
        if (this.indexEndValue != null) {
            i3 = this.indexEndValue.hashCode();
        } else {
            i3 = 0;
        }
        int i9 = (i8 + i3) * 31;
        if (this.indexEndName != null) {
            i4 = this.indexEndName.hashCode();
        } else {
            i4 = 0;
        }
        int i10 = (i9 + i4) * 31;
        if (this.index != null) {
            i5 = this.index.hashCode();
        }
        return i10 + i5;
    }
}
