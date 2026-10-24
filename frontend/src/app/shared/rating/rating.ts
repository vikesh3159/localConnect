import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-rating',
  standalone: true,
  imports: [],
  templateUrl: './rating.html',
  styleUrl: './rating.scss',
})
export class RatingComponent {
  readonly rating = input<number>(0);
  readonly value = input<number>(0);
  readonly reviewCount = input<number>(0);
  readonly count = input<number>(0);

  protected readonly displayRating = computed(() => {
    const val = this.rating() || this.value() || 0;
    return val ? val.toFixed(1) : '4.5';
  });

  protected readonly displayCount = computed(() => {
    return this.reviewCount() || this.count() || 0;
  });
}
