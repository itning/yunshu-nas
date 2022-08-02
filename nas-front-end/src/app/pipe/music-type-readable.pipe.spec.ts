import { MusicTypeReadablePipe } from './music-type-readable.pipe';

describe('MusicTypeReadablePipe', () => {
  it('create an instance', () => {
    const pipe = new MusicTypeReadablePipe();
    expect(pipe).toBeTruthy();
  });
});
