package com.trips.planner.trip;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trips.planner.activity.ActivityRepository;
import com.trips.planner.activity.ActivityRequestPayload;
import com.trips.planner.activity.ActivityResponse;
import com.trips.planner.activity.ActivityService;
import com.trips.planner.activity.ActivityVO;
import com.trips.planner.link.LinkData;
import com.trips.planner.link.LinkRequestPayload;
import com.trips.planner.link.LinkResponse;
import com.trips.planner.link.LinkService;
import com.trips.planner.participant.ParticipantCreateResponse;
import com.trips.planner.participant.ParticipantDTO;
import com.trips.planner.participant.ParticipantRequestPayload;
import com.trips.planner.participant.ParticipantService;

@RestController
@RequestMapping("/trips")
public class TripController {

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private LinkService linkService;

    // Trip

    @PostMapping
    public ResponseEntity<TripCreateResponse> createTrip(@RequestBody TripRequestPayload payload) {
        TripVO newTrip = new TripVO(payload);

        this.tripRepository.save(newTrip);

        if (payload.emails_to_invite() != null && !payload.emails_to_invite().isEmpty()) {
            this.participantService.registerParticipantsToEvent(payload.emails_to_invite(), newTrip);
        }
        return ResponseEntity.ok(new TripCreateResponse(newTrip.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, TripVO>> getTripDetails(@PathVariable UUID id) {
        Optional<TripVO> trip = this.tripRepository.findById(id);

        if (trip.isPresent()) {
            TripVO tripVO = trip.get();
            Map<String, TripVO> response = new HashMap<>();
            response.put("trip", tripVO);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // @GetMapping("/{id}")
    // public ResponseEntity<TripVO> getTripDetails(@PathVariable UUID id) {
    // Optional<TripVO> trip = this.tripRepository.findById(id);

    // return trip.map(ResponseEntity::ok).orElseGet(() ->
    // ResponseEntity.notFound().build());
    // }

    @PutMapping("/{id}")
    public ResponseEntity<TripVO> updateTrip(@PathVariable UUID id, @RequestBody TripRequestPayload payload) {
        Optional<TripVO> trip = this.tripRepository.findById(id);

        if (trip.isPresent()) {
            TripVO rawTrip = trip.get();
            rawTrip.setStarts_at(LocalDateTime.parse(payload.starts_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setEnds_at(LocalDateTime.parse(payload.ends_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setDestination(payload.destination());
            this.tripRepository.save(rawTrip);

            return ResponseEntity.ok(rawTrip);
        }

        return trip.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/confirm")
    public ResponseEntity<TripVO> confirmTrip(@PathVariable UUID id) {
        Optional<TripVO> trip = this.tripRepository.findById(id);

        if (trip.isPresent()) {
            TripVO rawTrip = trip.get();
            rawTrip.setIsConfirmed(true);
            this.tripRepository.save(rawTrip);
            this.participantService.triggerConfirmationEmailToParticipants(id);

            return ResponseEntity.ok(rawTrip);
        }

        return trip.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Participant

    @PostMapping("/{id}/invite")
    public ResponseEntity<ParticipantCreateResponse> inviteParticipant(@PathVariable UUID id,
            @RequestBody ParticipantRequestPayload payload) {

        Optional<TripVO> trip = this.tripRepository.findById(id);

        if (trip.isPresent()) {
            TripVO rawTrip = trip.get();

            ParticipantCreateResponse participantResponse = this.participantService
                    .registerParticipantToEvent(payload.email(), rawTrip);

            if (rawTrip.getIsConfirmed())
                this.participantService.triggerConfirmationEmailToParticipant(payload.email());

            return ResponseEntity.ok(participantResponse);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<Map<String, List<ParticipantDTO>>> getAllParticipants(@PathVariable UUID id) {
        List<ParticipantDTO> participantList = this.participantService.getAllParticipantsFromEvent(id);

        if (participantList != null && !participantList.isEmpty()) {
            Map<String, List<ParticipantDTO>> response = new HashMap<>();
            response.put("participants", participantList);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    // Activity

    @PostMapping("/{id}/activities")
    public ResponseEntity<ActivityResponse> registerActivity(@PathVariable UUID id,
            @RequestBody ActivityRequestPayload payload) {

        Optional<TripVO> trip = this.tripRepository.findById(id);

        if (trip.isPresent()) {
            TripVO rawTrip = trip.get();

            LocalDateTime occursAt;
            try {
                // O padrão agora inclui o dia da semana e o fuso horário
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH 'GMT'",
                        Locale.ENGLISH);
                occursAt = LocalDateTime.parse(payload.occurs_at(), formatter);
            } catch (DateTimeParseException e) {
                return ResponseEntity.badRequest().body(null);
            }

            ActivityResponse activityResponse = this.activityService.registerActivity(
                    new ActivityRequestPayload(payload.title(), occursAt.toString()), rawTrip);

            return ResponseEntity.ok(activityResponse);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<Map<String, Object>> getAllActivities(@PathVariable UUID id) {
        Optional<TripVO> tripOpt = this.tripRepository.findById(id);

        if (!tripOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        TripVO trip = tripOpt.get();

        List<ActivityVO> allActivities = this.activityRepository.findByTripId(id);

        long differenceInDays = ChronoUnit.DAYS.between(trip.getStarts_at(), trip.getEnds_at()) + 1;

        List<Map<String, Object>> activities = new ArrayList<>();

        for (int daysToAdd = 0; daysToAdd < differenceInDays; daysToAdd++) {

            LocalDateTime dateToCompare = trip.getStarts_at().plusDays(daysToAdd);

            List<ActivityVO> activitiesOnDate = allActivities.stream()
                    .filter(activity -> activity.getOccursAt().toLocalDate().isEqual(dateToCompare.toLocalDate())) // Converte
                    // comparação
                    .collect(Collectors.toList());

            Map<String, Object> dayActivity = new HashMap<>();
            dayActivity.put("date", dateToCompare);
            dayActivity.put("activities", activitiesOnDate);

            activities.add(dayActivity);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("activities", activities);

        return ResponseEntity.ok(response);
    }

    // Link

    @PostMapping("/{id}/links")
    public ResponseEntity<LinkResponse> registerLink(@PathVariable UUID id, @RequestBody LinkRequestPayload payload) {

        Optional<TripVO> trip = this.tripRepository.findById(id);

        if (trip.isPresent()) {
            TripVO rawTrip = trip.get();

            LinkResponse linkResponse = this.linkService.registerLink(payload, rawTrip);

            return ResponseEntity.ok(linkResponse);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/links")
    public ResponseEntity<Map<String, List<LinkData>>> getAllLinks(@PathVariable UUID id) {
        List<LinkData> linkDataList = this.linkService.getAllLinksFromTrip(id);

        if (linkDataList != null && !linkDataList.isEmpty()) {
            Map<String, List<LinkData>> response = new HashMap<>();
            response.put("links", linkDataList);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    // @GetMapping("/{id}/links")
    // public ResponseEntity<List<LinkData>> getAllLinks(@PathVariable UUID id) {
    // List<LinkData> linkDataList = this.linkService.getAllLinksFromTrip(id);

    // if (linkDataList == null || linkDataList.isEmpty()) {
    // return ResponseEntity.noContent().build();
    // }

    // return ResponseEntity.ok(linkDataList);
    // }

}
