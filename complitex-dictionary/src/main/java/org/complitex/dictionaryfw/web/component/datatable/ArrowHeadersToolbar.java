///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.complitex.dictionaryfw.web.component.datatable;
//
//import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
//import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
//import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
//import org.apache.wicket.markup.html.WebMarkupContainer;
//
///**
// *
// * @author Artem
// */
//public class ArrowHeadersToolbar extends HeadersToolbar {
//
//    public ArrowHeadersToolbar(final DataTable<?> table, final ISortStateLocator stateLocator) {
//        super(table, stateLocator);
//    }
//
//    @Override
//    protected WebMarkupContainer newSortableHeader(String headerId, final String property, final ISortStateLocator locator) {
//        return new ArrowOrderByBorder(headerId, property, locator);
//    }
//}
