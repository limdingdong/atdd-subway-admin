package nextstep.subway.section.domain;

import nextstep.subway.station.domain.Station;

import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Embeddable
public class Sections implements Iterable<Section> {

    private static final int FIRST_INDEX = 0;

    @OneToMany(mappedBy = "line")
    private final List<Section> sections = new LinkedList<>();

    public List<Station> getStations() {
        List<Station> stations = new ArrayList<>();
        stations.add(getFirstStation());
        stations.addAll(getUpStations());
        return stations;
    }

    private Station getFirstStation() {
        return sections.get(FIRST_INDEX).getDownStation();
    }

    private List<Station> getUpStations() {
        return sections.stream()
                .map(Section::getUpStation)
                .collect(Collectors.toList());
    }

    public boolean contains(Section section) {
        return sections.contains(section);
    }

    public void add(Section section) {
        if (sections.isEmpty()) {
            sections.add(section);
            return;
        }
        validateAddable(section);
        // TODO : 등록구간 인덱스를 판별한다
        // TODO : 구간인덱스에 맞게 등록한다
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

    @Override
    public Iterator<Section> iterator() {
        return sections.iterator();
    }
}
