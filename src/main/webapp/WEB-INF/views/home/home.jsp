<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="p" tagdir="/WEB-INF/tags/page/tag" %>
<%@taglib prefix="layout" tagdir="/WEB-INF/tags/layouts" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="helper" tagdir="/WEB-INF/tags/helpers" %>
<%@ taglib prefix="component" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%--@elvariable id="data" type="org.springframework.data.domain.Page<io.lana.libman.core.book.BookInfo>"--%>

<layout:librarian>
    <jsp:attribute name="title">Home</jsp:attribute>
    <jsp:attribute name="body">
        <div class="container-fluid">
            <div class="page-header">
                <div class="row">
                    <div class="col-sm-6">
                        <h3>Books</h3>
                        <ol class="breadcrumb">
                            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/">Home</a></li>
                            <li class="breadcrumb-item active">Books</li>
                        </ol>
                    </div>
                    <div class="col-sm-6">
                        <!-- Bookmark Start-->
                        <div class="bookmark">
                            <ul>
                                <li>
                                    <a href="${pageContext.request.contextPath}/home" up-follow up-instant><i
                                            data-feather="book"></i></a>
                                </li>
                                <sec:authorize access="isAuthenticated()">
                                    <li><a href="${pageContext.request.contextPath}/me/borrowing" up-follow
                                           up-instant><i
                                            data-feather="inbox"></i></a></li>
                                </sec:authorize>
                                <li><a href="${pageContext.request.contextPath}/me/favorites" up-follow up-instant><i
                                        class="bookmark-search"
                                        data-feather="star"></i></a>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="container-fluid product-wrapper">
            <div class="product-grid">
                <div class="feature-products">
                    <div class="row m-b-10">
                        <div class="col-12 text-end">
                            <span class="f-w-600 m-r-5 mt-2"><component:total pageMeta="${data}"
                                                                              verbose="${true}"/></span>
                            <div class="select2-drpdwn-product select-options d-inline-block">
                                <component:sorting
                                        cssClass="btn-square" target="#grid"
                                        labels="Newest;Title;Total Book;Borrow Cost;Available Book;Updated At"
                                        values="createdAt,desc;title;booksCount,desc;borrowCost,desc;availableBooksCount,desc;updatedAt,desc"/>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-12">
                            <div class="pro-filter-sec">
                                <div class="product-search">
                                    <form method="get" up-target="#grid">
                                        <div class="form-group m-0">
                                            <helper:inherit-param excludes="query"/>
                                            <input class="form-control" type="search"
                                                   placeholder="Search for books..." name="query"
                                                   value="${param.query}"
                                                   up-autosubmit up-delay="400">
                                            <i class="fa fa-search"></i>
                                        </div>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="product-wrapper-grid pb-4" id="grid">
                    <div class="row">
                        <c:forEach items="${data.content}" var="item">
                            <div class="col-xl-3 col-sm-6 col-md-4">
                                <div class="card">
                                    <div class="product-box">
                                        <div class="product-img">
                                            <c:if test="${ item.availableBooksCount == 0 and item.booksCount != 0}">
                                                <div class="ribbon ribbon-danger">Not Available</div>
                                            </c:if>
                                            <c:if test="${item.booksCount == 0}">
                                                <div class="ribbon ribbon-primary">Preview</div>
                                            </c:if>
                                            <img class="img-fluid"
                                                 src="${(empty item.image ? '/static/images/book-default.png' : item.image)}"
                                                 alt="book cover">
                                            <div class="product-hover">
                                                <ul>
                                                    <li up-href="${pageContext.request.contextPath}/home/books/${item.id}"
                                                        up-layer="new" up-size="large" up-instant up-clickable
                                                        up-history="false">
                                                        <div><i class="icon-eye"></i></div>
                                                    </li>
                                                </ul>
                                            </div>
                                        </div>
                                        <div class="product-details">
                                            <a href="${pageContext.request.contextPath}/home/books/${item.id}"
                                               up-layer="new" up-size="large" up-instant up-history="false">
                                                <h4>${item.title}</h4>
                                                <c:if test="${not empty item.seriesName}">
                                                <small>In series: ${item.seriesName}</small>
                                            </c:if>
                                            </a>
                                            <c:if test="${not empty item.year}">
                                                <small>Published in ${item.year}</small>
                                            </c:if>
                                            <c:if test="${not empty item.authorName}">
                                                <p>${item.authorName}</p>
                                            </c:if>
                                            <c:if test="${empty item.authorName}">
                                                <p>Unknown Author</p>
                                            </c:if>
                                            <div class="product-price">
                                                <fmt:formatNumber type="currency" maxFractionDigits="2"
                                                                  value="${item.borrowCost}"/>
                                                <span class="small">per day</span>
                                                <span> - ${item.availableBooksCount} Available</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                        <c:if test="${empty data.content}">
                            <component:empty/>
                        </c:if>
                    </div>
                    <nav class="d-flex justify-content-center mt-2">
                        <component:pagination pageMeta="${data}" target="#grid"
                                              up="up-scroll='layer' up-transition='cross-fade'"/>
                    </nav>
                </div>
            </div>
        </div>
    </jsp:attribute>
</layout:librarian>
