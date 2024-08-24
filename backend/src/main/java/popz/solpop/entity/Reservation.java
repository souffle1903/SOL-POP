package popz.solpop.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "reservation")
public class Reservation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "reserve_id")
  private Integer reserveId;

  @ManyToOne
  @JoinColumn(name = "store_id")
  @JsonManagedReference
  private Store store;

  @ManyToOne
  @JoinColumn(name = "mem_id")
  @JsonManagedReference
  private Member member;

  @Column(name = "reserve_date")
  private LocalDate reserveDate;

  @Column(name = "reserve_time")
  private LocalTime reserveTime;


  @Column(name = "is_enter", nullable = false)
  private Boolean isEnter;

  //columnDefinition = "BOOLEAN DEFAULT false" 보다 PrePersist 사용 권장
  @PrePersist
  public void prePersist() {
    this.isEnter = this.isEnter != null && this.isEnter;
  }

  public interface MyReservation {
    Integer getReserveId();
    StoreInfo getStore();
    LocalDate getReserveDate();
    LocalTime getReserveTime();
    Boolean getIsEnter();

    interface StoreInfo {
      Integer getStoreId();
      String getStoreName();
      String getStorePlace();
    }
  }
}
