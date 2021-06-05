package nextstep.subway.section.domain;

import nextstep.subway.station.domain.Station;

import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Embeddable
public class Sections implements Iterable<Section> {

    @OneToMany(mappedBy = "line")
    private final List<Section> sections = new ArrayList<>();

    public List<Station> getStations() {
        return sections.stream()
                .map(Section::getStation)
                .collect(Collectors.toList());
    }

    public boolean contains(Section section) {
        return sections.contains(section);
    }

    public void add(Section section) {
        if (!contains(section)) {
            sections.add(section);
        }
    }

    @Override
    public Iterator<Section> iterator() {
        return sections.iterator();
    }
}
