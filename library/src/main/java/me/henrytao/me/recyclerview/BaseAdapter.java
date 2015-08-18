/*
 * Copyright 2015 "Henry Tao <hi@henrytao.me>"
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.henrytao.me.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by henrytao on 8/16/15.
 */
public abstract class BaseAdapter extends RecyclerView.Adapter {

  public abstract void onBindFooterViewHolder(RecyclerView.ViewHolder holder, int index);

  public abstract void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int index);

  public abstract RecyclerView.ViewHolder onCreateFooterViewHolder(LayoutInflater inflater, ViewGroup parent, int index);

  public abstract RecyclerView.ViewHolder onCreateHeaderViewHolder(LayoutInflater inflater, ViewGroup parent, int index);

  private static final int CHUNK_SIZE = 1000;

  private final RecyclerView.Adapter mBaseAdapter;

  private final int mFooterCount;

  private final int mHeaderCount;

  public BaseAdapter(RecyclerView.Adapter baseAdapter, int headerCount, int footerCount) {
    mBaseAdapter = baseAdapter;
    mHeaderCount = headerCount;
    mFooterCount = footerCount;
    mBaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
      @Override
      public void onChanged() {
        super.onChanged();
        notifyDataSetChanged();
      }

      @Override
      public void onItemRangeChanged(int positionStart, int itemCount) {
        super.onItemRangeChanged(positionStart, itemCount);
        notifyItemRangeChanged(positionStart, itemCount);
      }

      @Override
      public void onItemRangeInserted(int positionStart, int itemCount) {
        super.onItemRangeInserted(positionStart, itemCount);
        notifyItemRangeInserted(positionStart, itemCount);
      }

      @Override
      public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        super.onItemRangeMoved(fromPosition, toPosition, itemCount);
        notifyItemMoved(fromPosition, toPosition);
      }

      @Override
      public void onItemRangeRemoved(int positionStart, int itemCount) {
        super.onItemRangeRemoved(positionStart, itemCount);
        notifyItemRangeRemoved(positionStart, itemCount);
      }
    });
  }

  public BaseAdapter(RecyclerView.Adapter baseAdapter) {
    this(baseAdapter, 0, 0);
  }

  @Override
  public int getItemCount() {
    return getBaseItemCount() + getHeaderCount() + getFooterCount();
  }

  @Override
  public int getItemViewType(int position) {
    if (isHeaderView(position)) {
      return ItemViewType.HEADER.getValue() * getChunkSize() + getHeaderViewIndex(position);
    } else if (isFooterView(position)) {
      return ItemViewType.FOOTER.getValue() * getChunkSize() + getFooterViewIndex(position);
    } else if (isItemView(position)) {
      return ItemViewType.ITEM.getValue() * getChunkSize();
    }
    return ItemViewType.BLANK.getValue() * getChunkSize();
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    if (isFooterView(position)) {
      onBindFooterViewHolder(holder, getFooterViewIndex(position));
    } else if (isHeaderView(position)) {
      onBindHeaderViewHolder(holder, getHeaderViewIndex(position));
    } else if (isItemView(position)) {
      mBaseAdapter.onBindViewHolder(holder, getDataPosition(position));
    }
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    int index = viewType - viewType / getChunkSize() * getChunkSize();
    viewType = viewType / getChunkSize();
    ItemViewType itemViewType = ItemViewType.BLANK;
    if (viewType == ItemViewType.HEADER.getValue()) {
      itemViewType = ItemViewType.HEADER;
    } else if (viewType == ItemViewType.FOOTER.getValue()) {
      itemViewType = ItemViewType.FOOTER;
    } else if (viewType == ItemViewType.ITEM.getValue()) {
      itemViewType = ItemViewType.ITEM;
    }

    LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
    RecyclerView.ViewHolder viewHolder = null;
    switch (itemViewType) {
      case FOOTER:
        viewHolder = onCreateFooterViewHolder(layoutInflater, parent, index);
        break;
      case HEADER:
        viewHolder = onCreateHeaderViewHolder(layoutInflater, parent, index);
        break;
      case ITEM:
        return mBaseAdapter.onCreateViewHolder(parent, 0);
    }
    return viewHolder == null ? onCreateBlankViewHolder(layoutInflater, parent) : viewHolder;
  }

  public int getBaseItemCount() {
    return mBaseAdapter.getItemCount();
  }

  public int getDataPosition(int position) {
    return position - getHeaderCount();
  }

  public int getFooterCount() {
    return mFooterCount;
  }

  public int getHeaderCount() {
    return mHeaderCount;
  }

  public int getPosition(int dataPosition) {
    return dataPosition + getHeaderCount();
  }

  public RecyclerView.ViewHolder onCreateBlankViewHolder(LayoutInflater inflater, ViewGroup parent) {
    return new BlankHolder(new View(parent.getContext()));
  }

  protected int getChunkSize() {
    return CHUNK_SIZE;
  }

  protected int getFooterViewIndex(int position) {
    if (getFooterCount() == 0) {
      return -1;
    }
    int startPosition = getBaseItemCount() + getHeaderCount();
    int endPosition = startPosition + getFooterCount() - 1;
    if (position < startPosition || position > endPosition) {
      return -1;
    }
    return position - startPosition;
  }

  protected int getFooterViewPosition(int index) {
    if (getFooterCount() == 0 || index < 0 || index >= getFooterCount()) {
      return -1;
    }
    int startPosition = getBaseItemCount() + getHeaderCount();
    return startPosition + index;
  }

  protected int getHeaderViewIndex(int position) {
    if (getHeaderCount() == 0) {
      return -1;
    }
    int startPosition = 0;
    int endPosition = startPosition + getHeaderCount() - 1;
    if (position < startPosition || position > endPosition) {
      return -1;
    }
    return position;
  }

  protected int getHeaderViewPosition(int index) {
    if (getHeaderCount() == 0 || index < 0 || index >= getHeaderCount()) {
      return -1;
    }
    return index;
  }

  protected boolean isBlankView(int position) {
    return position < getItemCount() ? false : true;
  }

  protected boolean isFooterView(int position) {
    return getFooterViewIndex(position) >= 0;
  }

  protected boolean isHeaderView(int position) {
    return getHeaderViewIndex(position) >= 0;
  }

  protected boolean isItemView(int position) {
    return !isHeaderView(position) && !isFooterView(position) && !isBlankView(position);
  }

  public enum ItemViewType {
    BLANK(0), FOOTER(1), HEADER(2), ITEM(3);

    private final int mValue;

    ItemViewType(int value) {
      mValue = value;
    }

    public int getValue() {
      return mValue;
    }
  }

  public static class BaseHolder extends RecyclerView.ViewHolder {

    protected View mItemView;

    public BaseHolder(View itemView) {
      super(itemView);
      mItemView = itemView;
    }

    public View getItemView() {
      return mItemView;
    }

    public void setOnClickListener(View.OnClickListener listener) {
      itemView.setOnClickListener(listener);
    }
  }

  public static class BlankHolder extends BaseHolder {

    public BlankHolder(View itemView) {
      super(itemView);
    }
  }

  public static class FooterHolder extends BaseHolder {

    public FooterHolder(View itemView) {
      super(itemView);
    }
  }

  public static class HeaderHolder extends BaseHolder {

    public HeaderHolder(View itemView) {
      super(itemView);
    }
  }
}
