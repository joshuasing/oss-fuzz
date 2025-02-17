// Copyright 2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
///////////////////////////////////////////////////////////////////////////
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.dataformat.smile.SmileGenerator;
import com.fasterxml.jackson.dataformat.smile.databind.SmileMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.EnumSet;

/** This fuzzer targets the methods of SmileGenerator */
public class SmileGeneratorFuzzer {
  public static void fuzzerTestOneInput(FuzzedDataProvider data) {
    try {
      // Retrieve set of SmileGenerator.Feature
      EnumSet<SmileGenerator.Feature> featureSet = EnumSet.allOf(SmileGenerator.Feature.class);

      // Create and configure SmileMapper
      SmileMapper mapper =
          new SmileMapper(
              SmileFactory.builder()
                  .enable(data.pickValue(featureSet))
                  .disable(data.pickValue(featureSet))
                  .build());

      // Failsafe logic
      if (mapper == null) {
        return;
      }

      // Create and configure SmileGenerator
      SmileGenerator generator = ((SmileMapper)mapper).getFactory().createGenerator(new ByteArrayOutputStream());
      for (SmileGenerator.Feature feature : featureSet) {
        generator.configure(feature, data.consumeBoolean());
      }
      generator.writeStartObject();

      // Fuzz methods of SmileGenerator
      String value = null;
      byte[] byteArray = null;
      switch (data.consumeInt(1, 20)) {
        case 1:
          generator.writeRaw(data.consumeByte());
          break;
        case 2:
          byteArray = data.consumeRemainingAsBytes();
          generator.writeBytes(byteArray, 0, byteArray.length);
          break;
        case 3:
          int[] intArray = data.consumeInts(data.consumeInt(1, 5));
          generator.writeArray(intArray, 0, intArray.length);
          break;
        case 4:
          long[] longArray = data.consumeLongs(data.consumeInt(1, 5));
          generator.writeArray(longArray, 0, longArray.length);
          break;
        case 5:
          double[] doubleArray = new double[data.consumeInt(1, 5)];
          for (int i = 0; i < doubleArray.length; i++) {
            doubleArray[i] = data.consumeDouble();
          }
          generator.writeArray(doubleArray, 0, doubleArray.length);
          break;
        case 6:
          generator.writeString(data.consumeRemainingAsString());
          break;
        case 7:
          value = data.consumeRemainingAsString();
          generator.writeString(value.toCharArray(), 0, value.length());
          break;
        case 8:
          generator.writeString(new SerializedString(data.consumeRemainingAsString()));
          break;
        case 9:
          byteArray = data.consumeRemainingAsBytes();
          generator.writeRawUTF8String(byteArray, 0, byteArray.length);
          break;
        case 10:
          byteArray = data.consumeRemainingAsBytes();
          generator.writeUTF8String(byteArray, 0, byteArray.length);
          break;
        case 11:
          byteArray = data.consumeRemainingAsBytes();
          generator.writeBinary(new ByteArrayInputStream(byteArray), byteArray.length);
          break;
        case 12:
          generator.writeBoolean(data.consumeBoolean());
          break;
        case 13:
          generator.writeNull();
          break;
        case 14:
          generator.writeNumber(data.consumeInt());
          break;
        case 15:
          generator.writeNumber(data.consumeLong());
          break;
        case 16:
          generator.writeNumber(data.consumeDouble());
          break;
        case 17:
          generator.writeNumber(data.consumeFloat());
          break;
        case 18:
          generator.writeNumber(new BigDecimal(data.consumeLong()));
          break;
        case 19:
          generator.writeNumber(BigInteger.valueOf(data.consumeLong()));
          break;
        case 20:
          generator.writeNumber(data.consumeRemainingAsString());
          break;
      }

      generator.writeEndObject();
      generator.flush();
      generator.close();
    } catch (IOException | IllegalArgumentException | IllegalStateException e) {
      // Known exception
    }
  }
}
