package nextstep.subway.section.domain;

import nextstep.subway.station.domain.Station;

import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Embeddable
public class Sections implements Iterable<Section> {

    private static final int FIRST_INDEX = 0;
    private static final int FRONT_OF_SECTIONS = -1;

    @OneToMany(mappedBy = "line")
    @OrderBy("sequence ASC")
    private final List<Section> sections = new LinkedList<>();

    public List<Station> getStations() {
        List<Station> stations = new ArrayList<>();
        stations.add(getFirstStation());
        stations.addAll(getUpStations());
        return stations;
    }

    public Station getFirstStation() {
        return sections.get(FIRST_INDEX).getDownStation();
    }

    public Station getLastStation() {
        return sections.get(lastIndex()).getUpStation();
    }

    private boolean isFirstStop(Station station) {
        return getFirstStation().equals(station);
    }

    private boolean isLastStop(Station station) {
        return getLastStation().equals(station);
    }

    private int lastIndex() {
        return sections.size() - 1;
    }

    private List<Station> getUpStations() {
        return sections.stream()
                .map(Section::getUpStation)
                .collect(Collectors.toList());
    }

    private List<Station> getDownStations() {
        return sections.stream()
                .map(Section::getDownStation)
                .collect(Collectors.toList());
    }

    public boolean contains(Section section) {
        return sections.contains(section);
    }

    public void add(Section element) {
        if (sections.isEmpty()) {
            sections.add(element);
            return;
        }
        validateAddable(element);
        add(selectDivisionIndex(element), element);
        synchronizeSequence();
    }

    private void validateAddable(Section section) {
        if (isStationAllContains(section)) {
            throw new IllegalArgumentException("구간에 속한 모든 역이 노선에 포함되어 있습니다. 역 정보를 확인해주세요.");
        }
        if (isStationNotContains(section)) {
            throw new IllegalArgumentException("구간에 속한 모든 역이 노선에 포함되어 있지 않습니다. 역 정보를 확인해주세요.");
        }
    }

    private boolean isStationAllContains(Section section) {
        return getStations().contains(section.getUpStation()) && getStations().contains(section.getDownStation());
    }

    private boolean isStationNotContains(Section section) {
        return !getStations().contains(section.getUpStation()) && !getStations().contains(section.getDownStation());
    }

    private int selectDivisionIndex(Section element) {
        if (isFirstStop(element.getUpStation())) {
            return FRONT_OF_SECTIONS;
        }
        if (isLastStop(element.getUpStation())) {
            return lastIndex();
        }
        if (isLastStop(element.getDownStation())) {
            return sections.size();
        }
        if (getUpStations().contains(element.getDownStation())) {
            return getUpStations().indexOf(element.getDownStation()) + 1;
        }
        return getDownStations().indexOf(element.getUpStation()) - 1;
    }

    private void add(int index, Section element) {
        if (isOutOfEdge(index)) {
            addEdge(index, element);
            return;
        }
        divideSection(index, element);
    }

    private boolean isOutOfEdge(int index) {
        return index == FRONT_OF_SECTIONS || index == sections.size();
    }

    private void addEdge(int index, Section element) {
        if (index == FRONT_OF_SECTIONS) {
            sections.add(FIRST_INDEX, element);
            return;
        }
        sections.add(index, element);
    }

    private void divideSection(int index, Section element) {
        Section divisionTarget = sections.get(index);
        divisionTarget.divideDistance(element);
        if (divisionTarget.getUpStation().equals(element.getUpStation())) {
            divisionTarget.modifyUpStation(element.getDownStation());
            sections.add(index + 1, element);
        }
        if (divisionTarget.getDownStation().equals(element.getDownStation())) {
            divisionTarget.modifyDownStation(element.getUpStation());
            sections.add(index, element);
        }
    }

    public Section remove(Station station) {
        validateRemovable(station);
        Section removeTarget = selectRemoveTarget(station);
        mergeSection(removeTarget, station);
        remove(removeTarget);
        return removeTarget;
    }

    private void validateRemovable(Station element) {
        if (sections.size() == 1) {
            throw new IllegalStateException("구간은 최소 한 개 이상 존재해야 합니다.");
        }
        if (!getStations().contains(element)) {
            throw new IllegalArgumentException("삭제하고자 하는 역 정보가 존재하지 않습니다. 입력정보를 확인해주세요.");
        }
    }

    private Section selectRemoveTarget(Station element) {
        if (isFirstStop(element)) {
            return sections.get(FIRST_INDEX);
        }
        if (isLastStop(element)) {
            return sections.get(lastIndex());
        }
        int index = getDownStations().indexOf(element);
        return sections.get(index);
    }

    private void mergeSection(Section removeTarget, Station station) {
        if (isEdge(station)) {
            return;
        }
        int removeIndex = sections.indexOf(removeTarget);
        Section mergeTarget = sections.get(removeIndex - 1);
        mergeTarget.mergeUpStation(removeTarget);
    }

    private boolean isEdge(Station station) {
        return isFirstStop(station) || isLastStop(station);
    }

    private void remove(Section section) {
        sections.remove(section);
        synchronizeSequence();
    }

    private void synchronizeSequence() {
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            section.modifySequence(i);
        }
    }

    @Override
    public Iterator<Section> iterator() {
        return sections.iterator();
    }
}
