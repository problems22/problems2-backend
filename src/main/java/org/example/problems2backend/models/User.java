package org.example.problems2backend.models;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Document(collection="users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User
    implements UserDetails
{
    @Id
    private ObjectId id;

    @Indexed(unique = true)
    private String username;

    private String passwordHash;

    @Builder.Default
    private String role = "USER";

    private String avatar;

    @Builder.Default
    private LocalDateTime memberSince = LocalDateTime.now();

    @Builder.Default
    private Integer rankPoints = 0;

    private RankTitle title = RankTitle.RECRUIT;

    @Builder.Default
    private Boolean isBanned = false;

    @Builder.Default
    private Stats stats = new Stats();

    @Builder.Default
    private Integer weeklyPoints = 0;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {

        @Builder.Default
        private Integer totalAttempts = 0;

        @Builder.Default
        private Integer correctAnswers = 0;

        @Builder.Default
        private Integer incorrectAnswers = 0;

    }


     @Getter
     public enum RankTitle {
        RECRUIT(0),
        APPRENTICE(100),
        NOVICE(300),
        WARRIOR(600),
        ELITE_SOLDIER(1000),
        GLADIATOR(1500),
        CHAMPION(2100),
        VETERAN(2800),
        WARLORD(3600),
        MASTER_FIGHTER(4500),
        GRANDMASTER(5500),
        LEGEND(6600),
        SHADOW_SLAYER(7800),
        TITAN(9100),
        MYTHIC_CONQUEROR(10500),
        IMMORTAL_GUARDIAN(12000),
        CELESTIAL_KNIGHT(13600),
        GOD_OF_WAR(15300),
        ETERNAL_OVERLORD(17100),
        ASCENDED_DEITY(19000);

        private final int requiredPoints;

        RankTitle(int requiredPoints) {
            this.requiredPoints = requiredPoints;
        }

         public static RankTitle getTitleByPoints(int points) {
            RankTitle bestMatch = RECRUIT;
            for (RankTitle title : RankTitle.values()) {
                if (points >= title.getRequiredPoints()) {
                    bestMatch = title;
                } else {
                    break;
                }
            }
            return bestMatch;
        }
    }




    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }


    @Override
    public boolean isAccountNonLocked() {
        return !isBanned;
    }


    @Override
    public boolean isEnabled() {
        return !isBanned;
    }



}
