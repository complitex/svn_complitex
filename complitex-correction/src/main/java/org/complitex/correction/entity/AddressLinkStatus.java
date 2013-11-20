package org.complitex.correction.entity;

/**
 * @author Pavel Sknar
 */
public enum AddressLinkStatus implements LinkStatus {

    /* группа "неразрешимых" статусов, т.е. любой статус из группы указывает на то, что какая-то часть внутреннего адреса у записи не разрешена */
    CITY_UNRESOLVED(200L), STREET_TYPE_UNRESOLVED(237L), STREET_UNRESOLVED(201L), STREET_AND_BUILDING_UNRESOLVED(231L),
    BUILDING_UNRESOLVED(202L), APARTMENT_UNRESOLVED(250L),

    /* найдено больше одной записи адреса во внутреннем адресном справочнике */
    MORE_ONE_CITY(234L), MORE_ONE_STREET_TYPE(238L), MORE_ONE_STREET(235L), MORE_ONE_BUILDING(236L),
    MORE_ONE_APARTMENT(251L),

    /* Найдено более одной записи в коррекциях */
    MORE_ONE_CITY_CORRECTION(210L), MORE_ONE_STREET_TYPE_CORRECTION(239L), MORE_ONE_STREET_CORRECTION(211L),
    MORE_ONE_BUILDING_CORRECTION(228L), MORE_ONE_APARTMENT_CORRECTION(252L),

    ADDRESS_LINKED(299L);

    private Long id;

    private AddressLinkStatus(Long id) {
        this.id = id;
    }


    @Override
    public Long getId() {
        return id;
    }
}
