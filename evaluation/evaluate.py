"""evaluation script"""

import pandas as pd
import numpy as np
import click


@click.command()
@click.option(
    '--input', '-i', 'input_file',
    metavar='<path>',
    type=click.Path(exists=True, resolve_path=True, dir_okay=False, file_okay=True, writable=True),
    default=None,
    required=True,
    help='The input file path.',
)
def main(input_file: str) -> None:
    """main"""
    df_data = pd.read_csv(input_file)
    df_data = df_data.sort_values(by=['ntpTimeMillis'])
    timestamps = df_data['ntpTimeMillis']

    diff = np.diff(timestamps.values)
    print(diff.mean(), diff.min(), diff.max())
    print(diff)
    print(timestamps[diff.argmax()-10:diff.argmax()+10])

if __name__ == "__main__":
    #pylint: disable=E1120
    main()
    #pylint: enable=E1120
