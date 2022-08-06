import { DateReadablePipe } from './date-readable.pipe';

describe('DateReadablePipe', () => {
  it('create an instance', () => {
    const pipe = new DateReadablePipe();
    expect(pipe).toBeTruthy();
  });
});
