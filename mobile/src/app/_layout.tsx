import "@/styles/global.css";
import "@/utils/dayjsLocaleConfig";

import { Loading } from "@/components/loading";
import { Slot } from "expo-router";
import { StatusBar, View } from "react-native";

import {
  Arima_400Regular,
  Arima_500Medium,
  Arima_600SemiBold,
  useFonts,
} from "@expo-google-fonts/arima";

export default function Layout() {
  const [fontsLoaded] = useFonts({
    Arima_500Medium,
    Arima_400Regular,
    Arima_600SemiBold,
  });

  if (!fontsLoaded) {
    return <Loading />;
  }

  return (
    <View className="flex-1 bg-zinc-950">
      <StatusBar
        barStyle="light-content"
        backgroundColor="transparent"
        translucent
      />
      <Slot />
    </View>
  );
}
