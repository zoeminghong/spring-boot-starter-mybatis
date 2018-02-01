package com.zeros.mybatis.plugin;

import com.ecfront.dew.common.Page;

import java.io.Serializable;
import java.util.List;

/**
 * Created on 2018/1/31.
 *
 * @author 迹_Jason
 */
public class MybatisPageContext {
    private static final ThreadLocal<PageRequest> PAGE_REQUEST_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<Page> PAGE_THREAD_LOCAL = new ThreadLocal<>();

    public static void clearAll() {
        PAGE_THREAD_LOCAL.remove();
        PAGE_REQUEST_THREAD_LOCAL.remove();
    }

    public static MybatisPageContext.PageRequest getPageRequest() {
        return PAGE_REQUEST_THREAD_LOCAL.get();
    }

    public static void setPageRequest(MybatisPageContext.PageRequest pageRequest) {
        PAGE_REQUEST_THREAD_LOCAL.remove();
        if (pageRequest == null) {
            throw new IllegalArgumentException("pageRequest cannot be null");
        }
        if (pageRequest.getPageSize() == null) {
            throw new IllegalArgumentException("pageRequest.pageSize cannot be null");
        }
        if (pageRequest.getPageNumber() == null) {
            throw new IllegalArgumentException("pageRequest.pageNumber cannot be null");
        }
        if (pageRequest.getPageNumber() < 1) {
            throw new IllegalArgumentException("pageRequest.pageNumber must greater than 0");
        }
        if (pageRequest.getPageSize() < 1) {
            throw new IllegalArgumentException("pageRequest.pageSize must greater than 0");
        }
        PAGE_REQUEST_THREAD_LOCAL.set(pageRequest);
    }

    public static void removePageRequest() {
        PAGE_REQUEST_THREAD_LOCAL.remove();
    }

    static boolean setTotalSize(int totalSize) {
        if (PAGE_REQUEST_THREAD_LOCAL.get() != null) {
            int pageNumber = MybatisPageContext.PAGE_REQUEST_THREAD_LOCAL.get().getPageNumber();
            int pageSize = MybatisPageContext.PAGE_REQUEST_THREAD_LOCAL.get().getPageSize();
            int totalPage = totalSize / pageSize + (totalSize % pageSize > 0 ? 1 : 0);
            Page page = new Page();
            page.setPageTotal(totalPage);
            page.setPageNumber(pageNumber);
            page.setRecordTotal(totalSize);
            page.setPageSize(pageSize);
            PAGE_THREAD_LOCAL.set(page);
            return true;
        }
        return false;
    }

    static boolean setPageResult(List result) {
        if (PAGE_THREAD_LOCAL.get() != null) {
            PAGE_REQUEST_THREAD_LOCAL.remove();
            (PAGE_THREAD_LOCAL.get()).setObjects(result);
            return true;
        }
        return false;
    }

    static boolean pageable() {
        return PAGE_REQUEST_THREAD_LOCAL.get() != null;
    }

    public static <T> Page<T> getPage() {
        return (Page<T>) PAGE_THREAD_LOCAL.get();
    }

    public static class PageRequest implements Serializable {
        private static final long serialVersionUID = 4181643443678756993L;
        private Integer pageNumber = 1;
        private Integer pageSize = 10;

        public PageRequest() {
        }

        public PageRequest(Integer pageNumber, Integer pageSize) {
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
        }

        public Integer getPageNumber() {
            return pageNumber;
        }

        public void setPageNumber(Integer pageNumber) {
            this.pageNumber = pageNumber;
        }

        public Integer getPageSize() {
            return pageSize;
        }

        public void setPageSize(Integer pageSize) {
            this.pageSize = pageSize;
        }
    }

}
